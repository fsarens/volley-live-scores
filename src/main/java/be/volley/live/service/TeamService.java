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

    /** All active teams — used for game planning and ranking */
    public List<Team> getAllTeams() {
        return teamRepository.findByActiveTrueOrderByNameAsc();
    }

    /** All teams including inactive — used for admin team management */
    public List<Team> getAllTeamsIncludingInactive() {
        return teamRepository.findAllByOrderByActiveDescNameAsc();
    }

    public Optional<Team> getTeamById(String id) {
        return teamRepository.findById(id);
    }

    public Optional<Team> getTeamByCode(String code) {
        return teamRepository.findByCode(code);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public Team deactivate(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        team.setActive(false);
        return teamRepository.save(team);
    }

    public Team reactivate(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        team.setActive(true);
        return teamRepository.save(team);
    }

}
