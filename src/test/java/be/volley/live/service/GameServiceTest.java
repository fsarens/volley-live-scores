package be.volley.live.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.volley.live.model.*;
import be.volley.live.repository.GameRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);
    }

    @Test
    void createGame_setsStatusToScheduled() {
        Game game = newGame();
        game.setStatus(null);
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Game result = gameService.createGame(game);

        assertEquals(GameStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void getGamesByDate_returnsGamesForDate() {
        LocalDate date = LocalDate.of(2025, 10, 4);
        List<Game> games = List.of(newGame(), newGame());
        when(gameRepository.findByDateOrderByTimeBlockAscCourtAsc(date)).thenReturn(games);

        List<Game> result = gameService.getGamesByDate(date);

        assertEquals(2, result.size());
    }

    @Test
    void updateStatus_updatesAndSaves() {
        Game game = newGame();
        game.setId("abc");
        when(gameRepository.findById("abc")).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Game result = gameService.updateStatus("abc", GameStatus.IN_PROGRESS);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(gameRepository).save(game);
    }

    @Test
    void updateStatus_unknownId_throwsException() {
        when(gameRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> gameService.updateStatus("unknown", GameStatus.IN_PROGRESS));
    }

    @Test
    void deleteGame_callsRepository() {
        gameService.deleteGame("abc");
        verify(gameRepository).deleteById("abc");
    }

    private Game newGame() {
        Game g = new Game();
        g.setDate(LocalDate.of(2025, 10, 4));
        g.setTimeBlock(TimeBlock.BLOCK_10);
        g.setCourt(Court.A1);
        g.setAwayTeam("VC Leuven");
        return g;
    }

}
