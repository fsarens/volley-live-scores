package be.volley.live.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.volley.live.model.Court;
import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;
import be.volley.live.model.TimeBlock;

public interface GameRepository extends MongoRepository<Game, String> {

    List<Game> findByDate(String date);

    List<Game> findByDateAndStatus(String date, GameStatus status);

    List<Game> findByDateOrderByTimeBlockAscCourtAsc(String date);

    boolean existsByDateAndCourtAndTimeBlock(String date, Court court, TimeBlock timeBlock);

    boolean existsByDateAndHomeTeamCodeAndTimeBlock(String date, String homeTeamCode, TimeBlock timeBlock);

    List<Game> findByHomeTeamCode(String code);

}
