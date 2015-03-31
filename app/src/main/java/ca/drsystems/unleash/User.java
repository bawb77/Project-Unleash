package ca.drsystems.unleash;
/**
 * Created by SRoddick3160 on 1/31/2015.
 *
 * This is a static class used to hold information
 * for each user in the game.
 */

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class User implements Serializable{

    private static final long serialVersionUID = 43L;
    private double lat, lon;
    private boolean alive;
    private String name;
    private int number;


    public User(){
        alive = true;
        this.lat = 1.0;
        this.lon = 1.0;
        this.number = 255;
    }
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

    public String toString(){
        String ret = "";

        ret += number + "###" + lat + "#" + lon + "#" + alive;

        return ret;
    }

//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
//    {
//        ObjectInputStream.GetField fields;
//        fields = in.readFields();
//        lat = fields.get("lat", 0.0);
//        lon = fields.get("lon", 0.0);
//        name = (String)fields.get("x2", "");
//        number = fields.get("num", 0);
//        alive = fields.get("alive", false);
//        Log.v("OK", "READING THIS FIELD IM GOING DEEPER HOLY SHIT");
//    }
//
//    private void writeObject(ObjectOutputStream out) throws IOException
//    {
//        ObjectOutputStream.PutField fields;
//        fields = out.putFields();
//        fields.put("lat", lat);
//        fields.put("lon", lon);
//        fields.put("name", name);
//        fields.put("num", number);
//        fields.put("alive", alive);
//        out.writeFields();
//
//        Log.v("OK", "WRITING THIS FIELD I DON'T KNOW WHAT IM DOING");
//    }
//
//    private void readObjectNoData() throws ObjectStreamException{
//        Log.v("OK", "WTF IS GOING ON I DON'T EVEN KNOW");
//    }
}