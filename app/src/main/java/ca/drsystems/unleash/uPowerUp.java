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

/**
 * Created by BBaxter3160 on 3/16/2015.
 */
public class uPowerUp {
    Play playAct;
    GoogleMap mMap;
    private Map<Integer, Marker> powerUpList;
    private Map<Integer, CircleOptions> powerUpListCircle;
    Random rand;
    final int POWER_UP = 252;

    public uPowerUp(Play PlayAct,GoogleMap map, Map<Integer, Marker> PowerUpList,Map<Integer, CircleOptions> PowerUpListCircle){
        this.playAct = PlayAct;
        this.mMap = map;
        this.powerUpList = PowerUpList;
        this.powerUpListCircle = PowerUpListCircle;
        rand = new Random();
    }

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
    public void checkLocation(User u)
    {
        if(u.getAlive())
        {
            for(Integer id : powerUpListCircle.keySet()) {
                PowerUp pTemp = new PowerUp(id, Play.UserLocations.getMyUser(),true);
                if (playAct.IsLocationInCircle(u.getLatLng(), powerUpListCircle.get(id))) {
                    if (playAct.host){
                        increasePowerLevel();
                        playAct.hostService.sendToAll(id, pTemp);}
                    else {
                        playAct.clientDeviceService.send(POWER_UP, pTemp);
                        removePowerUp(id);
                    }
                }
            }
        }

    }

    public void removePowerUp(int id)
    {
        Marker rem_marker = powerUpList.get(id);
        powerUpList.remove(id);
        powerUpListCircle.remove(id);
        rem_marker.remove();
    }

    public void increasePowerLevel(){
        TextView power = (TextView)playAct.findViewById(R.id.tv_score);
        playAct.powerLevel++;
        power.setText("Power: " + playAct.powerLevel);
    }
    public void decreasePowerLevel(){
        TextView power = (TextView)playAct.findViewById(R.id.tv_score);
        playAct.powerLevel = 0;
        power.setText("Power: " + playAct.powerLevel);
    }

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
    private CircleOptions GetMarkerBounds(Marker marker){
        double meters = 0.00003;

        CircleOptions circle_opt = new CircleOptions();
        circle_opt.center(marker.getPosition());
        circle_opt.radius(meters);

        return circle_opt;
    }

}
