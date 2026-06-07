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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final GameService gameService;
    private final TeamService teamService;

    public AdminController(GameService gameService, TeamService teamService) {
        this.gameService = gameService;
        this.teamService = teamService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String date, Model model) {
        LocalDate localDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        String dateStr = GameService.toDateStr(localDate);

        model.addAttribute("date", localDate);
        model.addAttribute("games", gameService.getGamesByDate(dateStr));
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("timeBlocks", TimeBlock.values());
        model.addAttribute("courts", Court.values());
        model.addAttribute("newGame", new Game());
        model.addAttribute("colors", TeamColor.values());
        model.addAttribute("gameRulesList", GameRules.values());
        model.addAttribute("teamRulesMap", buildTeamRulesMap());
        return "admin/index";
    }

    @PostMapping("/games")
    public String createGame(
            @RequestParam String date,
            @RequestParam String homeTeamCode,
            @RequestParam String awayTeam,
            @RequestParam TeamColor awayColor,
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
            model.addAttribute("colors", TeamColor.values());
            model.addAttribute("gameRulesList", GameRules.values());
            model.addAttribute("teamRulesMap", buildTeamRulesMap());
            return "admin/index";
        }

        return "redirect:/admin?date=" + date;
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
