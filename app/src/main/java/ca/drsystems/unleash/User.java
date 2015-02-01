package ca.drsystems.unleash;
/**
 * Created by SRoddick3160 on 1/31/2015.
 *
 * This is a static class used to hold information
 * for each user in the game.
 */
import java.io.Serializable;

public class User implements Serializable{

    private static final long serialVersionUID = 43L;
    private float lat;
    private float lon;
    private String name;
    private int number;

    public User(){

    }

    public float getLat(){

        return this.lat;
    }

    public float getLon(){
        return this.lon;
    }

    public String getName(){
        return this.name;
    }

    public int getNumber(){
        return this.number;
    }

    public void setLat(float l){
        this.lat = l;
    }

    public void setLon(float l){
        this.lon = l;
    }

    public void setName(String s){
        this.name = s;
    }

    public void setNumber(int n){
        this.number = n;
    }

    public String toString(){
        String ret = "";

        ret += number + "#" + name + '#' + lat + '#' + lon;

        return ret;
    }
}