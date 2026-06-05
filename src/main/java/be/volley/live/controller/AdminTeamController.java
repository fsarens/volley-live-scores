package be.volley.live.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import be.volley.live.model.League;
import be.volley.live.model.Sponsor;
import be.volley.live.model.Team;
import be.volley.live.repository.GameRepository;
import be.volley.live.service.TeamService;

@Controller
@RequestMapping("/admin/teams")
public class AdminTeamController {

    private final TeamService teamService;
    private final GameRepository gameRepository;

    private static final String[][] COLORS = {
        {"#333333", "Charcoal"}, {"#ffffff", "White"}, {"#1565c0", "Blue"},
        {"#c62828", "Red"}, {"#2e7d32", "Green"}, {"#e65100", "Orange"},
        {"#f9a825", "Yellow"}, {"#6a1b9a", "Purple"}
    };

    public AdminTeamController(TeamService teamService, GameRepository gameRepository) {
        this.teamService = teamService;
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teams", teamService.getAllTeamsIncludingInactive());
        return "admin/teams/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("leagues", League.values());
        model.addAttribute("colors", COLORS);
        return "admin/teams/form";
    }

    @PostMapping
    public String create(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam League league,
            @RequestParam(required = false) String reeks,
            @RequestParam String color,
            @RequestParam(required = false) String sponsor1Name,
            @RequestParam(required = false) String sponsor1Logo,
            @RequestParam(required = false) String sponsor2Name,
            @RequestParam(required = false) String sponsor2Logo,
            Model model) {

        Team team = new Team();
        team.setCode(code.toUpperCase().trim());
        team.setName(name.trim());
        team.setLeague(league);
        team.setReeks(reeks != null ? reeks.trim() : null);
        team.setColor(color);
        if (sponsor1Name != null && !sponsor1Name.isBlank()) {
            Sponsor s = new Sponsor(); s.setName(sponsor1Name); s.setLogo(sponsor1Logo);
            team.setSponsor(s);
        }
        if (sponsor2Name != null && !sponsor2Name.isBlank()) {
            Sponsor s = new Sponsor(); s.setName(sponsor2Name); s.setLogo(sponsor2Logo);
            team.setSponsor2(s);
        }

        try {
            teamService.save(team);
        } catch (Exception e) {
            model.addAttribute("error", "Could not save team: " + e.getMessage());
            model.addAttribute("team", team);
            model.addAttribute("leagues", League.values());
            model.addAttribute("colors", COLORS);
            return "admin/teams/form";
        }

        return "redirect:/admin/teams";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        Team team = teamService.getTeamById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        model.addAttribute("team", team);
        model.addAttribute("leagues", League.values());
        model.addAttribute("colors", COLORS);
        return "admin/teams/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable String id,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam League league,
            @RequestParam(required = false) String reeks,
            @RequestParam String color,
            @RequestParam(required = false) String sponsor1Name,
            @RequestParam(required = false) String sponsor1Logo,
            @RequestParam(required = false) String sponsor2Name,
            @RequestParam(required = false) String sponsor2Logo,
            Model model) {

        Team team = teamService.getTeamById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));

        team.setCode(code.toUpperCase().trim());
        team.setName(name.trim());
        team.setLeague(league);
        team.setReeks(reeks != null ? reeks.trim() : null);
        team.setColor(color);

        Sponsor s1 = sponsor1Name != null && !sponsor1Name.isBlank() ? new Sponsor() : null;
        if (s1 != null) { s1.setName(sponsor1Name); s1.setLogo(sponsor1Logo); }
        team.setSponsor(s1);

        Sponsor s2 = sponsor2Name != null && !sponsor2Name.isBlank() ? new Sponsor() : null;
        if (s2 != null) { s2.setName(sponsor2Name); s2.setLogo(sponsor2Logo); }
        team.setSponsor2(s2);

        teamService.save(team);

        // Cascade team changes to all games where this team is embedded as home team
        gameRepository.findByHomeTeamCode(team.getCode()).forEach(game -> {
            game.setHomeTeam(team);
            gameRepository.save(game);
        });

        return "redirect:/admin/teams";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable String id) {
        teamService.deactivate(id);
        return "redirect:/admin/teams";
    }

    @PostMapping("/{id}/reactivate")
    public String reactivate(@PathVariable String id) {
        teamService.reactivate(id);
        return "redirect:/admin/teams";
    }

}
