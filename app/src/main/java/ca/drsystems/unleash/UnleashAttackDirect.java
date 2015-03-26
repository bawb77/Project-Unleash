package ca.drsystems.unleash;

import java.io.Serializable;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class UnleashAttackDirect implements Serializable{
    private int player, powerLvl;
    long time;
    public double lat1,lon1,lat2,lon2;
    final int id = 250;
    public UnleashAttackDirect(int player, int powerLvl, double lat1, double lon1, double lat2, double lon2)
    {
        this.player = player;
        this.powerLvl = powerLvl;
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
        this.time = System.currentTimeMillis();
    }
    public int getPlayer(){return player;}
    public int getPowerLvl(){return powerLvl;}
    public double getLat1(){return lat1;}
    public double getLon1(){return lon1;}
    public double getLat2(){return lat2;}
    public double getLon2(){return lon2;}
    public long getTime(){return time;}
}
