/*

package game.players;

public class Clue
{
    public String word;
    public int number;

    public Clue(String word, int number) {
        this.word = word;
        this.number = number;
    }
}

*/

package game.players;

public class Clue {
    private final String word;
    private final int number;

    public Clue(String word, int number) {
        this.word = word;
        this.number = number;
    }

    public String getWord() {
        return word;
    }

    public int getNumber() {
        return number;
    }
}