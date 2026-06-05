package be.volley.live.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.volley.live.model.League;
import be.volley.live.model.Team;
import be.volley.live.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository);
    }

    @Test
    void getAllTeams_returnsOnlyActiveTeams() {
        Team da = team("DA", "Dames A", League.VVB);
        Team ha = team("HA", "Heren A", League.VVB);
        when(teamRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(da, ha));

        List<Team> result = teamService.getAllTeams();

        assertEquals(2, result.size());
    }

    @Test
    void getTeamByCode_existingCode_returnsTeam() {
        Team da = team("DA", "Dames A", League.VVB);
        when(teamRepository.findByCode("DA")).thenReturn(Optional.of(da));

        Optional<Team> result = teamService.getTeamByCode("DA");

        assertTrue(result.isPresent());
        assertEquals("DA", result.get().getCode());
    }

    @Test
    void getTeamByCode_unknownCode_returnsEmpty() {
        when(teamRepository.findByCode("XX")).thenReturn(Optional.empty());

        Optional<Team> result = teamService.getTeamByCode("XX");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_persistsTeam() {
        Team team = team("DA", "Dames A", League.VVB);
        when(teamRepository.save(team)).thenReturn(team);

        Team saved = teamService.save(team);

        assertEquals("DA", saved.getCode());
        verify(teamRepository).save(team);
    }

    private Team team(String code, String name, League league) {
        Team t = new Team();
        t.setCode(code);
        t.setName(name);
        t.setLeague(league);
        return t;
    }

}
