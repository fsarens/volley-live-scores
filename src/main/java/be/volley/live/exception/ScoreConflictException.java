package be.volley.live.exception;

import be.volley.live.model.Score;

public class ScoreConflictException extends RuntimeException {

    private final Score currentScore;

    public ScoreConflictException(Score currentScore) {
        super("Score conflict: expected version " + (currentScore.getVersion() - 1)
                + " but current version is " + currentScore.getVersion());
        this.currentScore = currentScore;
    }

    public Score getCurrentScore() {
        return currentScore;
    }

}
