package be.volley.live.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;

public interface GameRepository extends MongoRepository<Game, String> {

    List<Game> findByDate(LocalDate date);

    List<Game> findByDateAndStatus(LocalDate date, GameStatus status);

    List<Game> findByDateOrderByTimeBlockAscCourtAsc(LocalDate date);

}
