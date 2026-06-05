package be.volley.live.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "games")
public class Game {

    @Id
    private String id;

    private LocalDate date;
    private TimeBlock timeBlock;
    private Court court;

    private Team homeTeam;         // embedded — full WAVOC team
    private String awayTeam;       // visiting club name
    private String awayColor = "#c62828"; // preset by admin, used in scoring UI

    private GameStatus status = GameStatus.SCHEDULED;

    public Game() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

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

}
