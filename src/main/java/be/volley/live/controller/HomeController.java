package be.volley.live.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import be.volley.live.service.GameService;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameService.getGamesByDate(GameService.toDateStr(LocalDate.now())));
        return "home/index";
    }
}
