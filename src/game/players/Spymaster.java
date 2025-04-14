package game.players;

public abstract class Spymaster {
    public abstract Clue giveClue(int num) throws Exception;

    /**
     * clear previous prints
     */
    public static void clearScreen() {
        for (int i = 0; i < 15; i++) System.out.println();
        System.out.flush();
    }
    
    // New method to update game state
    public void updateGameState(int teamScore, int opponentScore, int totalRevealed) {
        // Default implementation does nothing
    }
    
    // New method to record opponent moves
    public void recordOpponentMove(String word, boolean wasOurTeam) {
        // Default implementation does nothing
    }
}