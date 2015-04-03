package ca.drsystems.unleash;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

//Object for passing Circular unleash attacks
public class UnleashAttack implements Serializable{
    private int player, powerLvl;
    private long time;
    private double lat,lon;
    private final int id = 251;
    //constructor
    public UnleashAttack(int player, int powerLvl, double lat, double lon)
    {
        this.player = player;
        this.powerLvl = powerLvl;
        this.lat = lat;
        this.lon = lon;
        this.time = System.currentTimeMillis();
    }
    public int getPlayer(){return player;}
    public int getPowerLvl(){return powerLvl;}
    public double getLat(){return lat;}
    public double getLon(){return lon;}
    public long getTime(){return time;}
    public LatLng getLocation(){
        LatLng temp = new LatLng(lat,lon);
        return temp;
    }
}
