package ca.drsystems.unleash;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class UnleashAttack {
    private int player, powerLvl;
    long time;
    public float lat,lon;
    public void UnleashAttack(int player, int powerLvl, float lat, float lon)
    {
        this.player = player;
        this.powerLvl = powerLvl;
        this.lat = lat;
        this.lon = lon;
        this.time = System.currentTimeMillis();
    }
    public int getPlayer(){return player;}
    public int getPowerLvl(){return powerLvl;}
    public float getLat(){return lat;}
    public float getLon(){return lon;}
    public long getTime(){return time;}
}
