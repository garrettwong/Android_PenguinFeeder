package com.core.score;

/**
 * Created by GWM on 6/5/17.
 */

public class Score {
    private int userId;
    private double value;

    public Score(int userId, double value) {
        this.userId = userId;
        this.value = value;
    }

    public int getUserId() {
        return userId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
