package com.core.score;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by GWM on 6/5/17.
 */
public class ScoreCollection {
    private List<Score> scores;
    private Map<Integer, Score> scoreMap;

    public ScoreCollection() {
        scores = new LinkedList<>();
        scoreMap = new HashMap<>();
    }

    public void addScore(int userId, double score) {
        addScore(new Score(userId, score));
    }

    public void addScore(Score score) {
        if (!scoreMap.containsKey(score.getUserId())) {
            scores.add(score);
        }
        scoreMap.put(score.getUserId(), score);
    }

    public Score getScore(int userId) {
        if (!scoreMap.containsKey(userId)) {
            throw new NullPointerException(userId + " not found.");
        }

        return scores.get(userId);
    }
}
