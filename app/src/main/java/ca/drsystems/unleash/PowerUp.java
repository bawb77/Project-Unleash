package ca.drsystems.unleash;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class PowerUp implements Serializable {
    private static final long serialVersionUID = 43L;
    private double lat, lon;
    private long time;
    private int powerNum, player;
    private boolean status;
    private final int id = 252;
    //constructor, This Object is used to pass power up locations for both creation and destruction.
    //based on the status variable the device either creates a new power upo a the LATLNG or destroys and existing one.
    //the Player stored in the Object when the Status is to destroy the power up receives the increase in power level.
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
    public boolean getStatus(){return this.status;}
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
