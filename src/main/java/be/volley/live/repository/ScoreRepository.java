package be.volley.live.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.volley.live.model.Score;

public interface ScoreRepository extends MongoRepository<Score, String> {

    Optional<Score> findByGameId(String gameId);

}
