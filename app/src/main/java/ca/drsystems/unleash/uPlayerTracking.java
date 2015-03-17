package ca.drsystems.unleash;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BBaxter3160 on 3/16/2015.
 */
public class uPlayerTracking {
    Play playAct;
    GoogleMap mMap;
    Map<Integer, Marker> userPosition;

    public uPlayerTracking(Play PlayAct, GoogleMap map, Map<Integer, Marker> UserPosition){
        this.playAct = PlayAct;
        this.mMap = map;
        this.userPosition = UserPosition;
    }
    public void getUsersInformationThread()
    {
        while(true)
        {
            HashMap<Integer, User> users = Play.UserLocations.returnList();
            for(final User u : users.values()){
                Log.v("ALC2", "user: " + u.getNumber() + " Loc: " + u.getLat());
                if(u.getLat() != 0.0 && u.getAlive()){
                    if(u.getNumber() != Play.UserLocations.getMyUser()){
                        Log.v("ALC2", "create marker for user: " + u.getNumber());
                        playAct.markopt = new MarkerOptions();
                        switch(u.getNumber())
                        {
                            case 0:
                                playAct.markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon3));
                                break;
                            case 1:
                                playAct.markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon1 ));
                                break;
                            case 2:
                                playAct.markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon2 ));
                                break;
                            case 3:
                                playAct.markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon4 ));
                                break;
                            default:
                                playAct.markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon3 ));
                                break;
                        }
                        Log.v("ALC2", "Marker created for: " + u.getNumber() + " at: " + u.getLat() + ", " + u.getLon());

                        playAct.handler.post(new Runnable(){
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                addUserMarker(playAct.markopt, u.getNumber());
                            }
                        });
                    }
                } else{
                    Log.v("ALC2", "marker not created: user " + u.getNumber() + "lat is 0.0!");
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public void addUserMarker(MarkerOptions in, Integer inNum)
    {
        Marker new_mark = mMap.addMarker(playAct.markopt);
        Log.v("ALC2", "Marker Added");
        if(userPosition.containsKey(inNum))
        {
            Marker curr_mark = userPosition.get(inNum);
            curr_mark.remove();
            userPosition.remove(inNum);
            Log.v("ALC", "Marker removed");
        }
        userPosition.put(inNum, new_mark);
    }
}
