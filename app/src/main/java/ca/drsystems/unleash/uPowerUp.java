package ca.drsystems.unleash;

import android.os.Handler;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.Random;

//these are methods pulled out of PLAY to reduce it's size
public class uPowerUp {
    public Play playAct;
    public GoogleMap mMap;
    private Map<Integer, Marker> powerUpList;
    private Map<Integer, CircleOptions> powerUpListCircle;
    private Random rand;
    private final int POWER_UP = 252;
    //Constructor
    public uPowerUp(Play PlayAct,GoogleMap map, Map<Integer, Marker> PowerUpList,Map<Integer, CircleOptions> PowerUpListCircle){
        this.playAct = PlayAct;
        this.mMap = map;
        this.powerUpList = PowerUpList;
        this.powerUpListCircle = PowerUpListCircle;
        rand = new Random();
    }
    //start the power ups spawning on a 10 second timer at random points within the boundaries of the map
    public void startSpawn()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                LatLng rand_point = getRandomPoint();
                int marker_id = powerUpList.size();
                makePowerUp(marker_id, rand_point);
                PowerUp send = new PowerUp(rand_point.latitude,rand_point.longitude,marker_id, Play.UserLocations.getMyUser(),false);
                playAct.hostService.sendToAll(POWER_UP, send);
                startSpawn();
            }
        }, 10000);
    }
    //create the power up marker to be displayed on the map
    public int makePowerUp(int marker_id, LatLng in)
    {
        Marker test = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(in.latitude, in.longitude))
                .title("Human Sacrifice")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.powericon)));
        test.setVisible(true);
        powerUpList.put(marker_id, test);
        powerUpListCircle.put(marker_id, GetMarkerBounds(test));
        return marker_id;
    }
    //check to see if player is within the surrounding area of a power up marker and send a power up object to the host if true.
    public void checkLocation(User u)
    {
        //make sure player is still in the game
        if(u.getAlive())
        {
            final Map<Integer, CircleOptions> temp = powerUpListCircle;
            //check all power ups to see if player is close to any of them
            for(Integer id : temp.keySet()) {
                PowerUp pTemp = new PowerUp(id, Play.UserLocations.getMyUser(),true);
                if (playAct.IsLocationInCircle(u.getLatLng(), powerUpListCircle.get(id))) {
                    //host logic
                    if (playAct.host){
                        increasePowerLevel();
                        playAct.hostService.sendToAll(id, pTemp);}
                    //client logic
                    else {
                        playAct.clientDeviceService.send(POWER_UP, pTemp);
                        removePowerUp(id);
                    }
                }
            }
        }
    }
    //remove a power up after the host has assigned to power to a player
    public void removePowerUp(int id)
    {
        Marker rem_marker = powerUpList.get(id);
        powerUpList.remove(id);
        powerUpListCircle.remove(id);
        rem_marker.remove();
    }
    //increase players power level
    public void increasePowerLevel(){
        TextView power = (TextView)playAct.findViewById(R.id.tv_score);
        playAct.powerLevel++;
        power.setText("Power: " + playAct.powerLevel);
    }
    //decrease a players power level after they unleash
    public void decreasePowerLevel(){
        TextView power = (TextView)playAct.findViewById(R.id.tv_score);
        playAct.powerLevel = 0;
        power.setText("Power: " + playAct.powerLevel);
    }
    //generate a random point within the current play field to place a power up marker at
    private LatLng getRandomPoint(){
        LatLng rand_point = null;

        LatLng ne = playAct.currScreen.northeast;
        LatLng sw = playAct.currScreen.southwest;

        double upper_lat = sw.latitude;
        double lower_lat = ne.latitude;
        double upper_lon = ne.longitude;
        double lower_lon = sw.longitude;

        double latitude = rand.nextDouble() * (upper_lat - lower_lat) + lower_lat;
        double longitude = rand.nextDouble() * (upper_lon - lower_lon) + lower_lon;

        rand_point = new LatLng(latitude, longitude);

        return rand_point;
    }
    // generate a circle around each marker 0.00003 across that is the zone for a player to pick up
    private CircleOptions GetMarkerBounds(Marker marker){
        double meters = 0.00003;

        CircleOptions circle_opt = new CircleOptions();
        circle_opt.center(marker.getPosition());
        circle_opt.radius(meters);

        return circle_opt;
    }

}
