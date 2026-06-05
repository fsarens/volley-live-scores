package be.volley.live.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import be.volley.live.model.Court;
import be.volley.live.model.Game;
import be.volley.live.model.GameStatus;
import be.volley.live.model.Score;
import be.volley.live.service.GameService;
import be.volley.live.service.ScoreService;

@Controller
@RequestMapping("/score")
public class ScorerController {

    private final GameService gameService;
    private final ScoreService scoreService;

    public ScorerController(GameService gameService, ScoreService scoreService) {
        this.gameService = gameService;
        this.scoreService = scoreService;
    }

    /** Game list for today */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameService.getGamesByDate(LocalDate.now()));
        return "score/index";
    }

    /** Score input screen for a specific game */
    @GetMapping("/{id}")
    public String game(@PathVariable String id, Model model) {
        Game game = gameService.getGame(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + id));

        model.addAttribute("game", game);
        model.addAttribute("gameId", id);
        model.addAttribute("courts", Court.values());

        // Preset colors for the color picker
        model.addAttribute("colors", new String[][]{
            {"#333333", "Charcoal"},
            {"#ffffff", "White"},
            {"#1565c0", "Blue"},
            {"#c62828", "Red"},
            {"#2e7d32", "Green"},
            {"#e65100", "Orange"},
            {"#f9a825", "Yellow"},
            {"#6a1b9a", "Purple"}
        });

        if (game.getStatus() == GameStatus.IN_PROGRESS || game.getStatus() == GameStatus.FINISHED) {
            scoreService.getScore(id).ifPresent(score -> model.addAttribute("score", score));
        }

        return "score/game";
    }

}
