package be.volley.live.repository;

import be.volley.live.model.DashboardToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DashboardTokenRepository extends MongoRepository<DashboardToken, String> {
    Optional<DashboardToken> findByToken(String token);
}
