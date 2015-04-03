package ca.drsystems.unleash;

import java.io.Serializable;

//EndGame Object for sending from host to Clients when all but one player have been eliminated
public class EndGame implements Serializable{
    private boolean gameState;
    private String player;

    //default constructor
    public EndGame()
    {
        this.gameState = false;
        //BB
        this.player = "Unnamed";

    }
    //Parameter Constructor
    public EndGame(boolean in, String Name)
    {
        this.gameState = in;
        this.player = Name;
    }
    //getters
    public boolean getState()
    {return gameState;}
    public String getPlayer()
    {
        return this.player;
    }
}
