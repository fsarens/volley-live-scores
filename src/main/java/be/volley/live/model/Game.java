package be.volley.live.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "games")
public class Game {

    @Id
    private String id;

    private String date;   // stored as "YYYYMMDD" — no timezone ambiguity
    private TimeBlock timeBlock;
    private Court court;

    private Team homeTeam;         // embedded — full WAVOC team
    private String awayTeam;       // visiting club name
    private String awayColor = "#c62828"; // preset by admin, used in scoring UI

    private GameStatus status = GameStatus.SCHEDULED;

    private GameRules gameRules = GameRules.YOUTH;

    public Game() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public TimeBlock getTimeBlock() { return timeBlock; }
    public void setTimeBlock(TimeBlock timeBlock) { this.timeBlock = timeBlock; }

    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public String getAwayColor() { return awayColor; }
    public void setAwayColor(String awayColor) { this.awayColor = awayColor; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public GameRules getGameRules() { return gameRules; }
    public void setGameRules(GameRules gameRules) { this.gameRules = gameRules; }

}
