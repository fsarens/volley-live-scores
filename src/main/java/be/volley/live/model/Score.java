package be.volley.live.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scores")
public class Score {

    @Id
    private String id;

    @Indexed(unique = true)
    private String gameId;

    private List<SetScore> sets = new ArrayList<>();  // completed sets

    private int currentSetHome;   // live score in current set
    private int currentSetAway;
    private int currentSet = 1;   // set number 1-5
    private boolean homeLeftSide = true;  // flips each set
    private int version = 0;              // incremented on every point for optimistic concurrency
    private boolean awaitingSet5SideChoice = false;  // true after set 4 ends; blocks scoring until sides chosen
    private boolean set5SideSwitched = false;         // true after the 8-point mid-set switch in set 5
    private boolean bonusSet = false;                 // true when playing youth rules bonus set 4 (does not count)
    private GameRules gameRules = GameRules.YOUTH;    // copied from Game at start; drives bonus set logic

    public Score() {}

    public Score(String gameId) {
        this.gameId = gameId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public List<SetScore> getSets() { return sets; }
    public void setSets(List<SetScore> sets) { this.sets = sets; }

    public int getCurrentSetHome() { return currentSetHome; }
    public void setCurrentSetHome(int currentSetHome) { this.currentSetHome = currentSetHome; }

    public int getCurrentSetAway() { return currentSetAway; }
    public void setCurrentSetAway(int currentSetAway) { this.currentSetAway = currentSetAway; }

    public int getCurrentSet() { return currentSet; }
    public void setCurrentSet(int currentSet) { this.currentSet = currentSet; }

    public boolean isHomeLeftSide() { return homeLeftSide; }
    public void setHomeLeftSide(boolean homeLeftSide) { this.homeLeftSide = homeLeftSide; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean isAwaitingSet5SideChoice() { return awaitingSet5SideChoice; }
    public void setAwaitingSet5SideChoice(boolean awaitingSet5SideChoice) { this.awaitingSet5SideChoice = awaitingSet5SideChoice; }

    public boolean isSet5SideSwitched() { return set5SideSwitched; }
    public void setSet5SideSwitched(boolean set5SideSwitched) { this.set5SideSwitched = set5SideSwitched; }

    public boolean isBonusSet() { return bonusSet; }
    public void setBonusSet(boolean bonusSet) { this.bonusSet = bonusSet; }

    public GameRules getGameRules() { return gameRules; }
    public void setGameRules(GameRules gameRules) { this.gameRules = gameRules; }

}
