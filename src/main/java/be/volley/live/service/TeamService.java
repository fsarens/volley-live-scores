package be.volley.live.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.volley.live.model.Team;
import be.volley.live.repository.TeamRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamByCode(String code) {
        return teamRepository.findByCode(code);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

}
