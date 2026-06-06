package be.volley.live.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.volley.live.exception.ScoreConflictException;
import be.volley.live.model.Game;
import be.volley.live.model.Score;
import be.volley.live.model.SetScore;
import be.volley.live.model.TimeBlock;
import be.volley.live.service.GameService;
import be.volley.live.service.ScoreService;

@RestController
@RequestMapping("/api/games")
public class ScoreController {

    private final GameService gameService;
    private final ScoreService scoreService;

    public ScoreController(GameService gameService, ScoreService scoreService) {
        this.gameService = gameService;
        this.scoreService = scoreService;
    }

    /** All games for today ordered by time block + court, optionally filtered by timeBlock */
    @GetMapping("/today")
    public List<Game> today(@RequestParam(required = false) String timeBlock) {
        List<Game> games = gameService.getGamesByDate(GameService.toDateStr(LocalDate.now()));
        if (timeBlock != null && !timeBlock.isBlank()) {
            try {
                TimeBlock tb = TimeBlock.valueOf(timeBlock);
                games = games.stream().filter(g -> tb.equals(g.getTimeBlock())).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) { }
        }
        return games;
    }

    /** Current score for a game */
    @GetMapping("/{id}/score")
    public ResponseEntity<Score> getScore(@PathVariable String id) {
        return scoreService.getScore(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Start a game — transition SCHEDULED → IN_PROGRESS, create Score */
    @PostMapping("/{id}/start")
    public Score startGame(@PathVariable String id, @RequestBody StartGameRequest request) {
        Score initial = new Score(id);
        initial.setHomeLeftSide(request.homeLeftSide());
        if (request.sets() != null) {
            initial.setSets(request.sets());
            initial.setCurrentSet(request.sets().size() + 1);
        }
        return scoreService.startGame(id, initial);
    }

    /** Add a point to the home team */
    @PostMapping("/{id}/point/home")
    public ResponseEntity<Score> addPointHome(@PathVariable String id,
                                               @RequestBody PointRequest request) {
        try {
            return ResponseEntity.ok(scoreService.addPointHome(id, request.version()));
        } catch (ScoreConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getCurrentScore());
        }
    }

    /** Add a point to the away team */
    @PostMapping("/{id}/point/away")
    public ResponseEntity<Score> addPointAway(@PathVariable String id,
                                               @RequestBody PointRequest request) {
        try {
            return ResponseEntity.ok(scoreService.addPointAway(id, request.version()));
        } catch (ScoreConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getCurrentScore());
        }
    }

    /** Undo last point */
    @PostMapping("/{id}/undo")
    public Score undo(@PathVariable String id) {
        return scoreService.undoLastPoint(id);
    }

    /** Set the sides for set 5 after the coin toss */
    @PostMapping("/{id}/set5side")
    public Score chooseSet5Side(@PathVariable String id, @RequestBody Set5SideRequest request) {
        return scoreService.chooseSet5Side(id, request.homeLeftSide());
    }

    // --- Request records ---

    record StartGameRequest(boolean homeLeftSide, List<SetScore> sets) {}

    record PointRequest(int version) {}

    record Set5SideRequest(boolean homeLeftSide) {}

}
