package ca.drsystems.unleash;

/**
 * Created by SRoddick3160 on 3/25/2015.
 */
public class EndGame {
    private boolean gameState;
    private String player;

    public EndGame()
    {
        this.gameState = false;
        this.player = "Unnamed";
    }
    public EndGame(boolean in, String Name)
    {
        this.gameState = in;
        this.player = Name;
    }
    public boolean getState()
    {return gameState;}
    public String getPlayer()
    {
        return this.player;
    }
}
