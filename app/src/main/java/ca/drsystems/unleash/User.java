package ca.drsystems.unleash;
import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;

//Object for passing User data between devices
public class User implements Serializable{

    private static final long serialVersionUID = 43L;
    private double lat, lon;
    private boolean alive;
    private String name;
    private int number;

    //default constructor
    public User(){
        alive = true;
        this.lat = 1.0;
        this.lon = 1.0;
        this.number = 255;
    }
    //Transfer Constructor
    public User(User u)
    {
        this.lat = u.getLat();
        this.lon = u.getLon();
        this.number = u.getNumber();
        this.alive = u.getAlive();
    }
    public boolean getAlive(){return this.alive;}

    public void setAlive(boolean in){this.alive = in;}

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

    public LatLng getLatLng(){
        LatLng temp = new LatLng(this.lat,this.lon);
        return temp;}

    //overRided toString method for logs
    public String toString(){
        String ret = "";

        ret += number + "###" + lat + "#" + lon + "#" + alive;

        return ret;
    }
}