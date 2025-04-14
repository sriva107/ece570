package game.players;

public class GameStateTracker {
    private int teamScore;
    private int opponentScore;
    private int totalRevealed;
    
    public void update(int teamScore, int opponentScore, int totalRevealed) {
        this.teamScore = teamScore;
        this.opponentScore = opponentScore;
        this.totalRevealed = totalRevealed;
    }
    
    public int getTeamScore() {
        return teamScore;
    }
    
    public int getOpponentScore() {
        return opponentScore;
    }
    
    public int getTotalRevealed() {
        return totalRevealed;
    }
    
    public void reset() {
        teamScore = 0;
        opponentScore = 0;
        totalRevealed = 0;
    }
}