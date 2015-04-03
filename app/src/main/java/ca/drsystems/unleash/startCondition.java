package ca.drsystems.unleash;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;

//created by the host to send out starting area for map
//created by the client to signal readiness
public class startCondition implements Serializable {
    private static final long serialVersionUID = 43L;
    private boolean ready;
    private double xur, xll, yur, yll;
    private int number;
    private final double EMPTY = 0.0;

    final int id = 254;
    //Constructor for the Host
    public startCondition(boolean ready, int number, double xur,double yur,double xll,double yll)
    {
        this.ready = ready;
        this.number = number;
        this.xur = xur;
        this.xll = xll;
        this.yur = yur;
        this.yll = yll;
    }
    //Constructor for the Client
    public startCondition(boolean ready, int number)
    {
        this.ready = ready;
        this.number = number;
        this.xur = EMPTY;
        this.xll = EMPTY;
        this.yur = EMPTY;
        this.yll = EMPTY;
    }
    public boolean getReady()
    {
        return ready;
    }
    public void getReady(boolean in){this.ready = in;}
    public int getNumber()
    {
        return number;
    }
    public void setNumber(int in){this.number = in;}
    public LatLngBounds mapSet()
    {
        LatLng NE = new LatLng(xur,yur);
        LatLng SW = new LatLng(xll,yll);
        return new LatLngBounds(SW,NE);
    }

}
