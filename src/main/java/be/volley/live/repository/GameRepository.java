package be.volley.live.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.volley.live.model.Court;
import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;
import be.volley.live.model.TimeBlock;

public interface GameRepository extends MongoRepository<Game, String> {

    List<Game> findByDate(LocalDate date);

    List<Game> findByDateAndStatus(LocalDate date, GameStatus status);

    List<Game> findByDateOrderByTimeBlockAscCourtAsc(LocalDate date);

    boolean existsByDateAndCourtAndTimeBlock(LocalDate date, Court court, TimeBlock timeBlock);

    boolean existsByDateAndHomeTeamCodeAndTimeBlock(LocalDate date, String homeTeamCode, TimeBlock timeBlock);

    List<Game> findByHomeTeamCode(String code);

}
