package ca.drsystems.unleash;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by BBaxter3160 on 2/16/2015.
 */
public class PowerUp implements Serializable {
    private static final long serialVersionUID = 43L;
    private double lat, lon;
    long time;
    private int powerNum, player;
    boolean status;
    final int id = 252;

    public PowerUp(double lat, double lon, int powerNum, int player, boolean status)
    {
        this.lat = lat;
        this.lon = lon;
        this.powerNum = powerNum;
        this.player = player;
        this.status = status;
        this.time = System.currentTimeMillis();
    }
    public PowerUp(int powerNum, int player, boolean status){
        this.powerNum = powerNum;
        this.player = player;
        this.status = status;
    }
    public double getLat()
    {
        return lat;
    }
    public double getLon()
    {
       return lon;
    }
    public int getPowerNum()
    {
        return powerNum;
    }
    public int getPlayer()
    {
        return player;
    }
    public long getTime(){return time;}
    public LatLng getLatLng(){
        LatLng temp = new LatLng(this.lat,this.lon);
        return temp;
    }
}
