package be.volley.live.model;

public class SetScore {

    private int home;
    private int away;

    public SetScore() {}

    public SetScore(int home, int away) {
        this.home = home;
        this.away = away;
    }

    public int getHome() { return home; }
    public void setHome(int home) { this.home = home; }

    public int getAway() { return away; }
    public void setAway(int away) { this.away = away; }

}
