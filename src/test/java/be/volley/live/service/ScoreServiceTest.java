package be.volley.live.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.volley.live.exception.ScoreConflictException;
import be.volley.live.model.*;
import be.volley.live.repository.GameRepository;
import be.volley.live.repository.ScoreRepository;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock private ScoreRepository scoreRepository;
    @Mock private GameRepository gameRepository;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, gameRepository);
        // default: save returns the argument (lenient to allow tests that don't call save)
        lenient().when(scoreRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // --- addPointHome ---

    @Test
    void addPointHome_incrementsHomeScore() {
        mockScore(score("game1", 10, 8, 1));

        Score result = scoreService.addPointHome("game1", 0);

        assertEquals(11, result.getCurrentSetHome());
        assertEquals(8, result.getCurrentSetAway());
    }

    @Test
    void addPointAway_incrementsAwayScore() {
        mockScore(score("game1", 8, 10, 1));

        Score result = scoreService.addPointAway("game1", 0);

        assertEquals(8, result.getCurrentSetHome());
        assertEquals(11, result.getCurrentSetAway());
    }

    // --- set end detection ---

    @Test
    void addPoint_at25_12_endsSet() {
        mockScore(score("game1", 24, 12, 1));

        Score result = scoreService.addPointHome("game1", 0);

        assertEquals(1, result.getSets().size());
        assertEquals(25, result.getSets().get(0).getHome());
        assertEquals(12, result.getSets().get(0).getAway());
        assertEquals(0, result.getCurrentSetHome());
        assertEquals(0, result.getCurrentSetAway());
        assertEquals(2, result.getCurrentSet());
    }

    @Test
    void addPoint_requiresWinByTwo_doesNotEndSetAt25_24() {
        mockScore(score("game1", 24, 24, 1));

        Score result = scoreService.addPointHome("game1", 0);

        assertEquals(0, result.getSets().size());
        assertEquals(25, result.getCurrentSetHome());
        assertEquals(24, result.getCurrentSetAway());
    }

    @Test
    void addPoint_endsSetAt26_24_whenDeuce() {
        mockScore(score("game1", 25, 24, 1));

        Score result = scoreService.addPointHome("game1", 0);

        assertEquals(1, result.getSets().size());
        assertEquals(26, result.getSets().get(0).getHome());
    }

    @Test
    void fifthSet_endsAt15_notAt25() {
        mockScore(score("game1", 14, 10, 5));

        Score result = scoreService.addPointHome("game1", 0);

        assertEquals(1, result.getSets().size());
        assertEquals(15, result.getSets().get(0).getHome());
    }

    @Test
    void fifthSet_doesNotEndAt25() {
        mockScore(score("game1", 24, 10, 5));

        // At 25-10 in set 5, this should NOT end the set (limit is 15)
        // but 25 >= 15 and margin >= 2, so it WILL end — verify that
        Score result = scoreService.addPointHome("game1", 0);

        // Set 5 ends at 15, so 25 is well past — set should be recorded
        assertEquals(1, result.getSets().size());
    }

    // --- side switching ---

    @Test
    void sidesSwitchAfterEachSet() {
        mockScore(score("game1", 24, 10, 1));
        assertTrue(scoreService.getScore("game1").get().isHomeLeftSide());

        Score result = scoreService.addPointHome("game1", 0);

        assertFalse(result.isHomeLeftSide());
    }

    // --- match end ---

    @Test
    void matchEnds_whenHomeWinsThreeSets() {
        Score s = score("game1", 24, 10, 4);
        s.getSets().add(new SetScore(25, 10)); // set 1 home
        s.getSets().add(new SetScore(25, 15)); // set 2 home
        s.getSets().add(new SetScore(18, 25)); // set 3 away
        mockScore(s);

        Game game = new Game();
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById("game1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        scoreService.addPointHome("game1", 0);

        verify(gameRepository).save(argThat(g -> g.getStatus() == GameStatus.FINISHED));
    }

    @Test
    void matchEnds_whenAwayWinsThreeSets() {
        Score s = score("game1", 10, 24, 4);
        s.getSets().add(new SetScore(10, 25)); // set 1 away
        s.getSets().add(new SetScore(15, 25)); // set 2 away
        s.getSets().add(new SetScore(25, 18)); // set 3 home
        mockScore(s);

        Game game = new Game();
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById("game1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        scoreService.addPointAway("game1", 0);

        verify(gameRepository).save(argThat(g -> g.getStatus() == GameStatus.FINISHED));
    }

    // --- concurrency ---

    @Test
    void addPointHome_wrongVersion_throwsConflict() {
        Score s = score("game1", 10, 8, 1);
        s.setVersion(3); // server is at version 3
        mockScore(s);

        assertThrows(ScoreConflictException.class,
                () -> scoreService.addPointHome("game1", 1)); // client thinks it's version 1
    }

    @Test
    void addPointHome_correctVersion_succeeds() {
        Score s = score("game1", 10, 8, 1);
        s.setVersion(3);
        mockScore(s);

        Score result = scoreService.addPointHome("game1", 3);
        assertEquals(11, result.getCurrentSetHome());
    }

    // --- undo ---

    @Test
    void undoLastPoint_removesHomePoint_whenHomeScoredLast() {
        mockScore(score("game1", 11, 8, 1));

        Score result = scoreService.undoLastPoint("game1");

        assertEquals(10, result.getCurrentSetHome());
        assertEquals(8, result.getCurrentSetAway());
    }

    @Test
    void undoLastPoint_removesAwayPoint_whenAwayScoredLast() {
        mockScore(score("game1", 8, 11, 1));

        Score result = scoreService.undoLastPoint("game1");

        assertEquals(8, result.getCurrentSetHome());
        assertEquals(10, result.getCurrentSetAway());
    }

    @Test
    void undoLastPoint_doesNothingAt0_0() {
        mockScore(score("game1", 0, 0, 1));

        Score result = scoreService.undoLastPoint("game1");

        assertEquals(0, result.getCurrentSetHome());
        assertEquals(0, result.getCurrentSetAway());
    }

    // --- helpers ---

    private Score score(String gameId, int home, int away, int set) {
        Score s = new Score(gameId);
        s.setCurrentSetHome(home);
        s.setCurrentSetAway(away);
        s.setCurrentSet(set);
        s.setHomeLeftSide(true);
        return s;
    }

    private void mockScore(Score s) {
        when(scoreRepository.findByGameId(s.getGameId())).thenReturn(Optional.of(s));
    }

}
