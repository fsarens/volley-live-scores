package be.volley.live.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String timeBlock, Model model) {
        model.addAttribute("timeBlock", timeBlock != null ? timeBlock : "");
        return "dashboard/index";
    }

}
