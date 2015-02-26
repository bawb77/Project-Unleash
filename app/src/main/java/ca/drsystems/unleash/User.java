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
    private double lat, lon;
    private String name;
    private int number;
    final int id = 253;


    public User(){

    }

    public double getLat(){ return this.lat; }

    public double getLon(){
        return this.lon;
    }

    public String getName(){
        return this.name;
    }

    public int getNumber(){
        return this.number;
    }

    public void setLat(double l){
        this.lat = l;
    }

    public void setLon(double l){
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