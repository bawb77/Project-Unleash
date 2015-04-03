package ca.drsystems.unleash;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

//Methods originally from play but relocated here to reduce PLAY size
public class uPlayerTracking {
    public Play playAct;
    public GoogleMap mMap;
    public Map<Integer, Marker> userPosition;

    //Constructor
    public uPlayerTracking(Play PlayAct, GoogleMap map, Map<Integer, Marker> UserPosition){
        this.playAct = PlayAct;
        this.mMap = map;
        this.userPosition = UserPosition;
    }
    //this method updates the map with the players location based on static method in play that is updated by the Services
    public void getUsersInformationThread()
    {
        while(true)
        {
            //pull List into temp variables to avoid Concurrency errors
            HashMap<Integer, User> users = Play.UserLocations.returnList();
            //foreach player
            for(final User u : users.values()){
                Log.v("ALC2", "user: " + u.getNumber() + " Loc: " + u.getLat() + " Alive: " + u.getAlive());
                //if their user data isn't set to default, they are still alive and not the player for whom the device is running
                if(u.getLat() != 1.0 && u.getAlive()&& u.getNumber() != Play.UserLocations.getMyUser()){
                    Log.v("ALC2", "create marker for user: " + u.getNumber());
                    //create marker information
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
                        //create the marker on the main thread
                        addUserMarker(playAct.markopt, u.getNumber());
                        }
                        });
                    }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //Add marker method
    public void addUserMarker(MarkerOptions in, Integer inNum)
    {
        //add the new marker
        Marker new_mark = mMap.addMarker(playAct.markopt);
        Log.v("ALC2", "Marker Added");
        //If player already existed on the map
        if(userPosition.containsKey(inNum))
        {
            //remove the old marker and update the UserPositions
            Marker curr_mark = userPosition.get(inNum);
            curr_mark.remove();
            userPosition.remove(inNum);
            Log.v("ALC", "Marker removed");
        }
        //add marker to userPositions to keep track of it
        userPosition.put(inNum, new_mark);
    }
}
