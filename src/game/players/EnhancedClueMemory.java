package game.players;

import java.util.HashMap;
import java.util.Map;

public class EnhancedClueMemory {

    private final Map<String, Integer> wordUsageCount = new HashMap<>();
    private final Map<String, Double> wordPerformance = new HashMap<>();
    
    public void recordClueUsage(String word, boolean successful) {
        wordUsageCount.put(word, wordUsageCount.getOrDefault(word, 0) + 1);
        // Update performance metrics
        double currentPerf = wordPerformance.getOrDefault(word, 1.0);
        double newPerf = successful ? currentPerf * 1.1 : currentPerf * 0.9;
        wordPerformance.put(word, Math.min(Math.max(newPerf, 0.1), 2.0));
    }
    
    public double getWordPenalty(String word) {
        int usage = wordUsageCount.getOrDefault(word, 0);
        double performance = wordPerformance.getOrDefault(word, 1.0);
        return usage * (1.0/performance);
    }
}

