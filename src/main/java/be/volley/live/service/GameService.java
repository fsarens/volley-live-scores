package be.volley.live.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;
import be.volley.live.repository.GameRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(Game game) {
        if (gameRepository.existsByDateAndCourtAndTimeBlock(game.getDate(), game.getCourt(), game.getTimeBlock())) {
            throw new IllegalArgumentException(
                "Court " + game.getCourt() + " is already occupied at " + game.getTimeBlock().name().replace("BLOCK_", "") + ":00");
        }
        if (gameRepository.existsByDateAndHomeTeamCodeAndTimeBlock(game.getDate(), game.getHomeTeam().getCode(), game.getTimeBlock())) {
            throw new IllegalArgumentException(
                game.getHomeTeam().getName() + " already has a game at " + game.getTimeBlock().name().replace("BLOCK_", "") + ":00");
        }
        if (game.getHomeTeam().getColor() != null
                && game.getHomeTeam().getColor().equalsIgnoreCase(game.getAwayColor())) {
            throw new IllegalArgumentException(
                "Home and away team cannot have the same color");
        }
        game.setStatus(GameStatus.SCHEDULED);
        return gameRepository.save(game);
    }

    public List<Game> getGamesByDate(LocalDate date) {
        return gameRepository.findByDateOrderByTimeBlockAscCourtAsc(date);
    }

    public List<Game> getGamesByDateAndStatus(LocalDate date, GameStatus status) {
        return gameRepository.findByDateAndStatus(date, status);
    }

    public Optional<Game> getGame(String id) {
        return gameRepository.findById(id);
    }

    public Game updateStatus(String id, GameStatus status) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + id));
        game.setStatus(status);
        return gameRepository.save(game);
    }

    public void deleteGame(String id) {
        gameRepository.deleteById(id);
    }

}
