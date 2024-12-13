package com.example.follow_me.data;

import java.util.Map;

public class PredictionResponse {
    private String[] escalator_predictions;
    private Map<String, String> probabilities;

    public String[] getEscalatorPredictions() {
        return escalator_predictions;
    }

    public Map<String, String> getProbabilities() {
        return probabilities;
    }
}