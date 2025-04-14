package game.players;

public class AdaptiveSpymaster extends Spymaster {
    private final Spymaster aggressiveStrategy;
    private final Spymaster conservativeStrategy;
    private int teamScore;
    private int opponentScore;
    
    public AdaptiveSpymaster(Spymaster aggressive, Spymaster conservative, int team) {
        this.aggressiveStrategy = aggressive;
        this.conservativeStrategy = conservative;
    }
    
    public void updateScores(int teamScore, int opponentScore) {
        this.teamScore = teamScore;
        this.opponentScore = opponentScore;
    }
    
    @Override
    public Clue giveClue(int num) throws Exception {
        // Use aggressive strategy when behind or in late game
        if (teamScore <= opponentScore || (teamScore + opponentScore) > 15) {
            return aggressiveStrategy.giveClue(num);
        }
        // Use conservative strategy when ahead
        return conservativeStrategy.giveClue(num);
    }
}