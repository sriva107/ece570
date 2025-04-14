package game.players;

import java.util.HashMap;
import java.util.Map;

public class CluePerformanceTracker {
    private final Map<String, Double> wordPerformance = new HashMap<>();
    
    public double getWordAdjustment(String word) {
        return wordPerformance.getOrDefault(word, 1.0);
    }
    
    public void recordCluePerformance(String clueWord, int successfulGuesses, int totalGuesses) {
        double successRatio = totalGuesses > 0 ? (double) successfulGuesses / totalGuesses : 0.5;
        wordPerformance.put(clueWord, 0.5 + successRatio); // Scores between 0.5-1.5
    }
}