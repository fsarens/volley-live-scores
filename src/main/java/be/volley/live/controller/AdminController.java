package be.volley.live.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import be.volley.live.model.*;
import be.volley.live.service.GameService;
import be.volley.live.service.TeamService;
import be.volley.live.model.GameRules;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final GameService gameService;
    private final TeamService teamService;

    private static final String[][] COLORS = {
        {"#333333", "Charcoal"}, {"#ffffff", "White"}, {"#1565c0", "Blue"},
        {"#c62828", "Red"}, {"#2e7d32", "Green"}, {"#e65100", "Orange"},
        {"#f9a825", "Yellow"}, {"#6a1b9a", "Purple"}
    };

    public AdminController(GameService gameService, TeamService teamService) {
        this.gameService = gameService;
        this.teamService = teamService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String date, Model model) {
        // date param arrives as "YYYY-MM-DD" from <input type="date"> or redirect
        LocalDate localDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        String dateStr = GameService.toDateStr(localDate);  // "YYYYMMDD" for queries

        model.addAttribute("date", localDate);             // LocalDate for template display/nav
        model.addAttribute("games", gameService.getGamesByDate(dateStr));
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("timeBlocks", TimeBlock.values());
        model.addAttribute("courts", Court.values());
        model.addAttribute("newGame", new Game());
        model.addAttribute("colors", COLORS);
        model.addAttribute("gameRulesList", GameRules.values());
        model.addAttribute("teamRulesMap", buildTeamRulesMap());
        return "admin/index";
    }

    @PostMapping("/games")
    public String createGame(
            @RequestParam String date,       // "YYYY-MM-DD" from hidden form field
            @RequestParam String homeTeamCode,
            @RequestParam String awayTeam,
            @RequestParam String awayColor,
            @RequestParam TimeBlock timeBlock,
            @RequestParam Court court,
            @RequestParam GameRules gameRules,
            Model model) {

        LocalDate localDate = LocalDate.parse(date);
        String dateStr = GameService.toDateStr(localDate);

        Team homeTeam = teamService.getTeamByCode(homeTeamCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown team: " + homeTeamCode));

        Game game = new Game();
        game.setDate(dateStr);
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setAwayColor(awayColor);
        game.setTimeBlock(timeBlock);
        game.setCourt(court);
        game.setGameRules(gameRules);

        try {
            gameService.createGame(game);
        } catch (IllegalArgumentException e) {
            model.addAttribute("date", localDate);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("games", gameService.getGamesByDate(dateStr));
            model.addAttribute("teams", teamService.getAllTeams());
            model.addAttribute("timeBlocks", TimeBlock.values());
            model.addAttribute("courts", Court.values());
            model.addAttribute("newGame", game);
            model.addAttribute("colors", COLORS);
            model.addAttribute("gameRulesList", GameRules.values());
            model.addAttribute("teamRulesMap", buildTeamRulesMap());
            return "admin/index";
        }

        return "redirect:/admin?date=" + date;  // keep YYYY-MM-DD in URL
    }

    private Map<String, String> buildTeamRulesMap() {
        return teamService.getAllTeams().stream().collect(Collectors.toMap(
            t -> t.getCode(),
            t -> t.getGameRules() != null ? t.getGameRules().name() : "YOUTH"
        ));
    }

    @PostMapping("/games/{id}/delete")
    public String deleteGame(@PathVariable String id, @RequestParam String date) {
        gameService.deleteGame(id);
        return "redirect:/admin?date=" + date;
    }

}
