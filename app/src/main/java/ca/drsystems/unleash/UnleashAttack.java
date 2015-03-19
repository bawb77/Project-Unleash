package ca.drsystems.unleash;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class UnleashAttack {
    private int player, powerLvl;
    long time;
    public double lat,lon;

    final int id = 251;

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
