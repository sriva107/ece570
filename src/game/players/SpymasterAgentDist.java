package game.players;

import distance.BoardDistance;
import game.board.Board;
import game.board.Card;

import java.util.*;

public class SpymasterAgentDist extends Spymaster {
    private final Board board;
    private final Map<String, Map<String, Double>> distances;
    private final int team;
    private final String scoreFunction;
    private final List<String> used;
    private final EnhancedClueMemory clueMemory;
    private final OpponentGuessPredictor opponentPredictor;
    private boolean isAggressiveMode = false;
    private double lambda = 0.5; // Default value

    public SpymasterAgentDist(BoardDistance bd, Board board, int team, String scoreFunction,
                            EnhancedClueMemory memory, OpponentGuessPredictor predictor) {
        this.board = board;
        this.distances = bd.getBoardDistances();
        this.team = team;
        this.scoreFunction = scoreFunction;
        this.used = new ArrayList<>();
        this.clueMemory = memory;
        this.opponentPredictor = predictor;
    }

    // Simplified constructor for backward compatibility
    public SpymasterAgentDist(BoardDistance bd, Board board, int team, String scoreFunction) {
        this(bd, board, team, scoreFunction, new EnhancedClueMemory(), new OpponentGuessPredictor());
    }

    public void setAggressiveMode(boolean aggressive) {
        this.isAggressiveMode = aggressive;
        this.lambda = aggressive ? 0.3 : 0.7; // Adjust lambda based on strategy
    }

    @Override
    public void updateGameState(int teamScore, int opponentScore, int totalRevealed) {
        // Switch to aggressive if behind or in late game
        setAggressiveMode(teamScore <= opponentScore || totalRevealed > 15);
    }

    @Override
    public void recordOpponentMove(String word, boolean wasOurTeam) {
        opponentPredictor.recordOpponentGuess(word, wasOurTeam);
    }

    @Override
    public Clue giveClue(int num) {
        // Remove board words from possible clue words
        for (String word : board.getWords()) distances.remove(word);

        Map<String, Double[]> clueMap = switch (scoreFunction) {
            case "scoreRatio" -> scoreRatio(num);
            case "scoreDifference" -> scoreDifference(num);
            case "scoreKoyyalagunta" -> scoreKoyyalagunta(num, lambda);
            default -> null;
        };
        assert clueMap != null;

        // Apply enhancements to scores
        applyStrategyAdjustments(clueMap);

        // Find best clue
        String bestWord = findBestClue(clueMap);
        if (bestWord == null) {
            return handleNoValidClue();
        }

        int clueNum = clueMap.get(bestWord)[1].intValue();
        System.out.println("The spymaster's message is " + bestWord + ", " + clueNum);
        this.used.add(bestWord);
        return new Clue(bestWord, clueNum);
    }

    private void applyStrategyAdjustments(Map<String, Double[]> clueMap) {
        for (Map.Entry<String, Double[]> entry : clueMap.entrySet()) {
            String word = entry.getKey();
            Double[] values = entry.getValue();
            
            // Adjust score based on memory and opponent prediction
            double penalty = clueMemory.getWordPenalty(word);
            double risk = opponentPredictor.predictRisk(word);
            values[0] = values[0] * (1 - penalty) * (1 - risk);
            
            // Strategy-specific adjustments
            if (isAggressiveMode) {
                values[0] *= 1.2; // Boost score in aggressive mode
                if (values[1] > 1) {
                    values[0] *= 1.1; // Additional boost for multi-word clues
                }
            } else {
                // Conservative mode favors safer clues
                values[0] *= (1 - risk * 0.5);
            }
        }
    }

    private String findBestClue(Map<String, Double[]> clueMap) {
        String bestWord = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, Double[]> entry : clueMap.entrySet()) {
            if (entry.getValue()[0] > maxScore && !this.used.contains(entry.getKey())) {
                maxScore = entry.getValue()[0];
                bestWord = entry.getKey();
            }
        }
        return bestWord;
    }

    private Clue handleNoValidClue() {
        System.err.println("Warning: No valid clue found, using fallback");
        // Fallback: return a random word with number 1
        List<String> available = new ArrayList<>(distances.keySet());
        available.removeAll(used);
        if (available.isEmpty()) {
            available = new ArrayList<>(distances.keySet());
        }
        String fallbackWord = available.get(new Random().nextInt(available.size()));
        return new Clue(fallbackWord, 1);
    }

    private Map<String, Double[]> scoreDifference(int num) {
        Map<String, Double[]> wordScores = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
            String word = entry.getKey();
            Map<String, Double> map = entry.getValue();

            List<Double> goodWordsDistances = new LinkedList<>();
            Double minBadDistance = Double.POSITIVE_INFINITY;

            for (Card card : board.getCards()) {
                if (!card.isRevealed()) {
                    String cWord = card.getWord();
                    if (card.getColor() == team) {
                        goodWordsDistances.add((map.get(cWord) == null) ? Double.POSITIVE_INFINITY : map.get(cWord));
                    } else if (map.get(cWord) != null && minBadDistance > map.get(cWord)) {
                        minBadDistance = map.get(cWord);
                    }
                }
            }

            Double finalMinBadDistance = minBadDistance;
            double score = 0.0;
            int number;

            if (num == -1) {
                goodWordsDistances.removeIf(dist -> dist > finalMinBadDistance - 0.02);
                number = goodWordsDistances.size();
                for (Double dist : goodWordsDistances) {
                    score += Math.min(minBadDistance - dist, 0.3);
                }
            } else {
                Collections.sort(goodWordsDistances);
                number = Math.min(num, goodWordsDistances.size());
                score = number == 0 ? Double.NEGATIVE_INFINITY : minBadDistance - goodWordsDistances.get(number-1);
            }

            wordScores.put(word, new Double[]{score, (double) number});
        }

        return wordScores;
    }

    private Map<String, Double[]> scoreRatio(int num) {
        Map<String, Double[]> wordScores = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
            String word = entry.getKey();
            Map<String, Double> map = entry.getValue();

            List<Double> goodWordsDistances = new LinkedList<>();
            Double minBadDistance = Double.POSITIVE_INFINITY;

            for (Card card : board.getCards()) {
                if (!card.isRevealed()) {
                    String cWord = card.getWord();
                    if (card.getColor() == team) {
                        goodWordsDistances.add((map.get(cWord) == null) ? Double.POSITIVE_INFINITY : map.get(cWord));
                    } else if (map.get(cWord) != null && minBadDistance > map.get(cWord)) {
                        minBadDistance = map.get(cWord);
                    }
                }
            }

            Double finalMinBadDistance = minBadDistance;
            double score = 0.0;
            int number;

            if (num == -1) {
                goodWordsDistances.removeIf(dist -> dist > finalMinBadDistance - 0.02);
                number = goodWordsDistances.size();
                for (Double dist : goodWordsDistances) {
                    score += Math.min(minBadDistance / dist, 5);
                }
            } else {
                Collections.sort(goodWordsDistances);
                number = Math.min(num, goodWordsDistances.size());
                score = number == 0 ? Double.NEGATIVE_INFINITY : minBadDistance / goodWordsDistances.get(number-1);
            }

            wordScores.put(word, new Double[]{score, (double) number});
        }

        return wordScores;
    }

    private Map<String, Double[]> scoreKoyyalagunta(int num, double lambda) {
        Map<String, Double[]> wordScores = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
            String word = entry.getKey();
            Map<String, Double> distanceMap = entry.getValue();

            List<Double> goodWordsDistances = new LinkedList<>();
            Double minBadDistance = Double.POSITIVE_INFINITY;

            for (Card card : board.getCards()) {
                if (!card.isRevealed()) {
                    String cardWord = card.getWord();
                    if (card.getColor() == team) {
                        goodWordsDistances.add((distanceMap.get(cardWord) == null) ? Double.POSITIVE_INFINITY : distanceMap.get(cardWord));
                    } else if (distanceMap.get(cardWord) != null && minBadDistance > distanceMap.get(cardWord)) {
                        minBadDistance = distanceMap.get(cardWord);
                    }
                }
            }

            Double finalMinBadDistance = minBadDistance;
            goodWordsDistances.removeIf(dist -> dist > finalMinBadDistance);

            double score = 0.0;
            int number;

            if (num == -1) {
                number = goodWordsDistances.size();
                for (Double dist : goodWordsDistances) {
                    score += dist;
                }
                score -= lambda * (1 - minBadDistance);
            } else {
                Collections.sort(goodWordsDistances);
                number = Math.min(num, goodWordsDistances.size());
                for (int i = 0; i < number; i++) {
                    score += 1 - goodWordsDistances.get(i);
                }
                score -= lambda * (1 - minBadDistance);
            }

            wordScores.put(word, new Double[]{score, (double) number});
        }

        return wordScores;
    }

    public static void clearScreen() {
        Spymaster.clearScreen();
    }
}