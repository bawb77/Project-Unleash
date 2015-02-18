package ca.drsystems.unleash;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class UnleashAttackDirect {
    private int player, powerLvl;
    long time;
    public float lat1,lon1,lat2,lon2;
    public void UnleashAttack(int player, int powerLvl, float lat1, float lon1, float lat2, float lon2)
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
    public float getLat1(){return lat1;}
    public float getLon1(){return lon1;}
    public float getLat2(){return lat2;}
    public float getLon2(){return lon2;}
    public long getTime(){return time;}
}
