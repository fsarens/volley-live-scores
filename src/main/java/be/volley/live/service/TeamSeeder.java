package be.volley.live.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.volley.live.model.Team;
import be.volley.live.repository.TeamRepository;

@Component
public class TeamSeeder implements ApplicationRunner {

    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TeamSeeder(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (teamRepository.count() > 0) {
            return;
        }

        File teamsFile = new File("data/teams.json");
        if (!teamsFile.exists()) {
            System.out.println("No data/teams.json found — skipping team seed");
            return;
        }

        try {
            List<Team> teams = objectMapper.readValue(teamsFile, new TypeReference<>() {});
            teamRepository.saveAll(teams);
            System.out.println("Seeded " + teams.size() + " teams from teams.json");
        } catch (IOException e) {
            System.err.println("Failed to seed teams: " + e.getMessage());
        }
    }

}
