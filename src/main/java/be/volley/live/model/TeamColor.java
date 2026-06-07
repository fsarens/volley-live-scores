package be.volley.live.model;

public enum TeamColor {
    WHITE("#ffffff"),
    BLUE("#42a5f5"),
    RED("#ef5350"),
    GREEN("#66bb6a"),
    ORANGE("#ff7043"),
    YELLOW("#ffca28"),
    PURPLE("#ab47bc"),
    CYAN("#26c6da");

    private final String hex;

    TeamColor(String hex) { this.hex = hex; }

    public String getHex() { return hex; }
}
