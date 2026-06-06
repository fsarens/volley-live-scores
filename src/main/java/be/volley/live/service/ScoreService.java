package be.volley.live.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import be.volley.live.exception.ScoreConflictException;
import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;
import be.volley.live.model.Score;
import be.volley.live.model.SetScore;
import be.volley.live.repository.GameRepository;
import be.volley.live.repository.ScoreRepository;

@Service
public class ScoreService {

    private static final int SETS_TO_WIN = 3;
    private static final int POINTS_TO_WIN_SET = 25;
    private static final int POINTS_TO_WIN_FINAL_SET = 15;
    private static final int FINAL_SET = 5;
    private static final int MIN_WIN_MARGIN = 2;

    private final ScoreRepository scoreRepository;
    private final GameRepository gameRepository;

    public ScoreService(ScoreRepository scoreRepository, GameRepository gameRepository) {
        this.scoreRepository = scoreRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Start tracking a game. If joining late, caller can set initial set scores.
     */
    public Score startGame(String gameId, Score initialScore) {
        // Update game status to IN_PROGRESS
        gameRepository.findById(gameId).ifPresent(game -> {
            game.setStatus(GameStatus.IN_PROGRESS);
            gameRepository.save(game);
        });

        Score score = initialScore != null ? initialScore : new Score(gameId);
        score.setGameId(gameId);
        return scoreRepository.save(score);
    }

    public Optional<Score> getScore(String gameId) {
        return scoreRepository.findByGameId(gameId);
    }

    /**
     * Add a point to the home team.
     * expectedVersion is used for optimistic concurrency — throws ScoreConflictException on mismatch.
     */
    public Score addPointHome(String gameId, int expectedVersion) {
        Score score = getOrThrow(gameId);
        checkVersion(score, expectedVersion);
        if (score.isAwaitingSet5SideChoice()) {
            throw new IllegalStateException("Choose set 5 sides before scoring");
        }
        score.setCurrentSetHome(score.getCurrentSetHome() + 1);
        return checkSetEnd(score, gameId);
    }

    /**
     * Add a point to the away team.
     * expectedVersion is used for optimistic concurrency — throws ScoreConflictException on mismatch.
     */
    public Score addPointAway(String gameId, int expectedVersion) {
        Score score = getOrThrow(gameId);
        checkVersion(score, expectedVersion);
        if (score.isAwaitingSet5SideChoice()) {
            throw new IllegalStateException("Choose set 5 sides before scoring");
        }
        score.setCurrentSetAway(score.getCurrentSetAway() + 1);
        return checkSetEnd(score, gameId);
    }

    /**
     * Set the sides for set 5 after the coin toss.
     */
    public Score chooseSet5Side(String gameId, boolean homeLeftSide) {
        Score score = getOrThrow(gameId);
        score.setHomeLeftSide(homeLeftSide);
        score.setAwaitingSet5SideChoice(false);
        score.setVersion(score.getVersion() + 1);
        return scoreRepository.save(score);
    }

    /**
     * Undo the last point. Removes a point from whoever scored last
     * based on the current set score. Falls back to the home team if tied.
     */
    public Score undoLastPoint(String gameId) {
        Score score = getOrThrow(gameId);

        if (score.getCurrentSetHome() == 0 && score.getCurrentSetAway() == 0) {
            return score; // nothing to undo
        }

        // Determine who scored last: higher score was last to score
        // If equal, assume home scored last (conservative)
        if (score.getCurrentSetHome() >= score.getCurrentSetAway()
                && score.getCurrentSetHome() > 0) {
            score.setCurrentSetHome(score.getCurrentSetHome() - 1);
        } else if (score.getCurrentSetAway() > 0) {
            score.setCurrentSetAway(score.getCurrentSetAway() - 1);
        }

        score.setVersion(score.getVersion() + 1);
        return scoreRepository.save(score);
    }

    /**
     * Check whether the current set has ended after a point is added.
     * If so, record the set, switch sides, and advance to next set (or finish game).
     */
    private Score checkSetEnd(Score score, String gameId) {
        // Check for set 5 mid-set side switch (at 8 points)
        checkSet5SideSwitch(score);

        int home = score.getCurrentSetHome();
        int away = score.getCurrentSetAway();
        int setNumber = score.getCurrentSet();
        int pointsToWin = (setNumber == FINAL_SET) ? POINTS_TO_WIN_FINAL_SET : POINTS_TO_WIN_SET;

        boolean homeWinsSet = home >= pointsToWin && (home - away) >= MIN_WIN_MARGIN;
        boolean awayWinsSet = away >= pointsToWin && (away - home) >= MIN_WIN_MARGIN;

        if (homeWinsSet || awayWinsSet) {
            // Record completed set
            score.getSets().add(new SetScore(home, away));
            score.setCurrentSetHome(0);
            score.setCurrentSetAway(0);
            score.setCurrentSet(setNumber + 1);

            // Set 5: scorer chooses sides after coin toss — do NOT auto-flip
            if (score.getCurrentSet() == FINAL_SET) {
                score.setAwaitingSet5SideChoice(true);
            } else {
                score.setHomeLeftSide(!score.isHomeLeftSide());
            }

            // Check if match is over
            long homeSetsWon = score.getSets().stream().filter(s -> s.getHome() > s.getAway()).count();
            long awaySetsWon = score.getSets().stream().filter(s -> s.getAway() > s.getHome()).count();

            if (homeSetsWon == SETS_TO_WIN || awaySetsWon == SETS_TO_WIN) {
                finishGame(gameId);
            }
        }

        return scoreRepository.save(score);
    }

    /**
     * In set 5, switch sides when either team first reaches 8 points.
     */
    private void checkSet5SideSwitch(Score score) {
        if (score.getCurrentSet() != FINAL_SET) return;
        if (score.isSet5SideSwitched()) return;
        if (score.getCurrentSetHome() >= 8 || score.getCurrentSetAway() >= 8) {
            score.setHomeLeftSide(!score.isHomeLeftSide());
            score.setSet5SideSwitched(true);
        }
    }

    private void finishGame(String gameId) {
        gameRepository.findById(gameId).ifPresent(game -> {
            game.setStatus(GameStatus.FINISHED);
            gameRepository.save(game);
        });
    }

    private Score getOrThrow(String gameId) {
        return scoreRepository.findByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Score not found for game: " + gameId));
    }

    private void checkVersion(Score score, int expectedVersion) {
        if (score.getVersion() != expectedVersion) {
            throw new ScoreConflictException(score);
        }
    }

}
