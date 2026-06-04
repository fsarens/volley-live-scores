package be.volley.live.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.volley.live.model.Team;

public interface TeamRepository extends MongoRepository<Team, String> {

    Optional<Team> findByCode(String code);

}
