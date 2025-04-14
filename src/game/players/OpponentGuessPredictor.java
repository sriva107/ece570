package game.players;

import java.util.HashMap;
import java.util.Map;

public class OpponentGuessPredictor {
    private final Map<String, Double> wordRiskScores = new HashMap<>();
    private double riskDecayFactor = 0.9; // How quickly old guesses become less relevant
    
    public void recordOpponentGuess(String word, boolean wasOurTeam) {
        double currentRisk = wordRiskScores.getOrDefault(word, 0.0);
        // Increase risk score if opponent guessed our word
        double adjustment = wasOurTeam ? 0.2 : -0.1;
        wordRiskScores.put(word, Math.min(Math.max(currentRisk + adjustment, 0.0), 1.0));
    }
    
    public void decayRiskScores() {
        wordRiskScores.replaceAll((k, v) -> v * riskDecayFactor);
    }
    
    public double predictRisk(String word) {
        return wordRiskScores.getOrDefault(word, 0.0);
    }
    
    public void reset() {
        wordRiskScores.clear();
    }
}