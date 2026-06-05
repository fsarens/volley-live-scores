package be.volley.live.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
    public String index(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) date = LocalDate.now();

        model.addAttribute("date", date);
        model.addAttribute("games", gameService.getGamesByDate(date));
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("timeBlocks", TimeBlock.values());
        model.addAttribute("courts", Court.values());
        model.addAttribute("newGame", new Game());
        return "admin/index";
    }

    @PostMapping("/games")
    public String createGame(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String homeTeamCode,
            @RequestParam String awayTeam,
            @RequestParam TimeBlock timeBlock,
            @RequestParam Court court,
            Model model) {

        Team homeTeam = teamService.getTeamByCode(homeTeamCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown team: " + homeTeamCode));

        Game game = new Game();
        game.setDate(date);
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setTimeBlock(timeBlock);
        game.setCourt(court);

        try {
            gameService.createGame(game);
        } catch (IllegalArgumentException e) {
            model.addAttribute("date", date);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("games", gameService.getGamesByDate(date));
            model.addAttribute("teams", teamService.getAllTeams());
            model.addAttribute("timeBlocks", TimeBlock.values());
            model.addAttribute("courts", Court.values());
            model.addAttribute("newGame", game);
            return "admin/index";
        }

        return "redirect:/admin?date=" + date;
    }

    @PostMapping("/games/{id}/delete")
    public String deleteGame(@PathVariable String id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        gameService.deleteGame(id);
        return "redirect:/admin?date=" + date;
    }

}
