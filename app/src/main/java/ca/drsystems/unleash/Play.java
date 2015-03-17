package ca.drsystems.unleash;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Play extends FragmentActivity implements WifiP2pManager.ConnectionInfoListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public joinFrag jf = new joinFrag();
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    private final IntentFilter intentFilter = new IntentFilter();
    private List<WifiP2pDevice> peersAvailable = new ArrayList();
    private Map<Integer, Marker> powerUpList;
    private Map<Integer, CircleOptions> powerUpListCircle;
    private Map<Integer, Marker> userPosition;
    private boolean isWifiP2pEnabled;

    LocationListener locationListener;

    ClientDeviceService clientDeviceService;
    public static final LocationRequest locationRequest = new LocationRequest()
            .create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500);
    GoogleApiClient myGoogleApiClient;

    final HashMap<String, String> buddies = new HashMap<String, String>();
    HostService hostService;
    Channel mChannel;
    Context context;
    TextView numPlayer;
    WifiP2pManager mManager;
    WifiDirectBroadcastReceiver receiver;
    PeerListListener peerListListener;
    WifiP2pServiceInfo serviceInfo;
    WifiP2pDnsSdServiceRequest serviceRequest;
    WifiP2pInfo gInfo;
    MarkerOptions markopt;
    Handler handler;
    LatLngBounds currScreen;
    Random rand;
    boolean deviceServiceStarted;
    boolean host;
    boolean run;
    int powerLevel;
    int connected;
    int tCount;
    int pCount;
    final int INITIAL_PACKET_NUMBER = 255;
    final int START_CONDITIONS = 254;
    final int USER_CLASS = 253;
    final int POWER_UP = 252;
    final int UNLEASH_C = 251;
    final int UNLEASH_D = 250;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        UserLocations.setMyUser(INITIAL_PACKET_NUMBER);
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        myGoogleApiClient.connect();
        powerUpList = new HashMap<Integer, Marker>();
        powerUpListCircle = new HashMap<Integer, CircleOptions>();
        userPosition = new HashMap<Integer, Marker>();
        rand = new Random();
        pCount = 0;
        powerLevel = 0;
        host = false;
        run = true;
        deviceServiceStarted = false;
        handler = new Handler();
        context = this;
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        initializeIntents();
        createPeerListListener();
        setUpMapIfNeeded();
        joinFragStart();


    }

    public void joinFragStart() {
        fragmentTransaction.replace(R.id.map, jf).commit();
    }

    public void initializeIntents() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    private void createPeerListListener() {
        Log.v("P2P", "Creating PeerListListener");
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                Log.v("P2P", "onPeersAvailable() call");
                // Out with the old, in with the new.
                peersAvailable.clear();
                peersAvailable.addAll(peerList.getDeviceList());

                Log.v("P2P", "peerList: " + peerList);
                numPlayer = (TextView) findViewById(R.id.numPlayers);
                numPlayer.setText("" + peersAvailable.size());
                for(WifiP2pDevice device : peersAvailable)
                {
                    if(!deviceServiceStarted || deviceServiceStarted && host)
                        connect(device);
                }
            }
        };
    }

    public void connect(WifiP2pDevice device) {

        final WifiP2pDevice device1 = device;
        if (device1.status == WifiP2pDevice.AVAILABLE) {

        Log.v("P2P", "Connecting to device: " + device.deviceName +
                " with address: " + device.deviceAddress);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device1.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.v("P2P", "Connection to: " + device1.deviceName +
                        " initiated");
                createGroupLogic();
            }
            @Override
            public void onFailure(int reason) {
                Log.v("P2P", "Connection to: " + device1.deviceName +
                        " initiation failed: " + reason);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(device1.status != 1)
                            connect(device1);
                    }
                }, 1000);
            }
        });

        }
    }


    public void createGroupLogic() {
        Log.v("P2P", "CreateGroupLogic");
        mManager.requestConnectionInfo(mChannel,
                new WifiP2pManager.ConnectionInfoListener() {

                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info.groupFormed) {
                            if (info.isGroupOwner && !deviceServiceStarted) {
                                startHostService();
                            } else if (!deviceServiceStarted) {
                                startClientDeviceService(info);
                            }
                        }
                        else{
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    createGroupLogic();
                                }
                            }, 1000);
                        }
                    }
                });
    }


    private void startHostService(){
        deviceServiceStarted = true;
        host = true;
        UserLocations.setMyUser(0);
        tCount = 0;
        connected = 0;
        Log.v("SOCK", "Starting HostService");
        hostService = new HostService(handler, this);
        hostService.execute();
        startLocationRequest();
    }


    private void startClientDeviceService(final WifiP2pInfo info){
        deviceServiceStarted = true;
        Log.v("SOCK", "Starting ClientDeviceService");
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Log.v("SOCK", "DeviceHolder info: " + info);
                    clientDeviceService = new ClientDeviceService(handler, Play.this, 12345, info.groupOwnerAddress);
                    clientDeviceService.execute();
                } catch (Exception e) {
                    Log.v("SOCK", "startClientListener: Exception");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        startLocationRequest();
    }



    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(50.671191, -120.363182)).title("Marker"));

    }


    private void initializeDiscovery() {

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.v("P2P", "Added service request!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.v("P2P", "Did not add service: " + reason);
                    }
                });

        mManager.discoverServices(mChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.v("P2P", "Peer Discovery Initialized");
                Toast.makeText(Play.this, "Looking for friends!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.v("P2P", "Peer Discovery Failed To Initialize");
                Toast.makeText(Play.this, "Error finding friends, code : " + reasonCode,
                        Toast.LENGTH_SHORT).show();

                if (reasonCode == WifiP2pManager.NO_SERVICE_REQUESTS) {

                    // initiate a stop on service discovery
                    mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // initiate clearing of the all service requests
                            mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // reset the service listeners, service requests, and discovery
                                    initializeDiscovery();
                                }
                                @Override
                                public void onFailure(int i) {
                                    Log.d("P2P", "FAILED to clear service requests ");
                                }
                            });
                        }
                        @Override
                        public void onFailure(int i) {
                            Log.d("P2P", "FAILED to stop discovery");
                        }
                    });
                }
            }
        });
    }


    public void letsPlay(boolean readyTemp, ToggleButton r, View v) {

        if(host)
        {
            Log.d("P2P", "***********************HOST SENDS STRCON********************");
            Log.v("P2p", "Counts" + tCount + ":" + connected);
            if(tCount == connected) {
                Log.v("P2P", "SHOULD BE STARTING NOW FUCK");
                LatLngBounds temp = mMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng temp2 = temp.northeast;
                LatLng temp3 = temp.southwest;
                startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser(),temp2.latitude, temp2.longitude, temp3.latitude,temp3.longitude);
                hostService.sendToAll(START_CONDITIONS, strCon);
                startGame(v);
                startSpawn();
            }
            else
            {
                r.setChecked(false);
            }
        }
        else if(!host)
        {
            Log.d("P2P", "***********************Client SENDS STRCON********************");
            startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser());
            clientDeviceService.send(START_CONDITIONS, strCon);
        }

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
                PowerUp send = new PowerUp(rand_point.latitude,rand_point.longitude,marker_id,UserLocations.getMyUser(),false);
                hostService.sendToAll(POWER_UP,send);
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
        for(Integer id : powerUpListCircle.keySet()) {
            PowerUp pTemp = new PowerUp(id, UserLocations.getMyUser(),true);
            if (IsLocationInCircle(u.getLatLng(), powerUpListCircle.get(id))) {
                if (host){
                    increasePowerLevel();
                    hostService.sendToAll(id, pTemp);}
                else {
                    clientDeviceService.send(POWER_UP, pTemp);
                    removePowerUp(id);
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
    public void increasePowerLevel()
    {
        powerLevel++;
    }

    private boolean IsLocationInCircle(LatLng location, CircleOptions circle){
        double lat = Math.abs(location.latitude) - Math.abs(circle.getCenter().latitude);
        double lon = Math.abs(location.longitude) - Math.abs(circle.getCenter().longitude);

        double diff = Math.sqrt((Math.pow(Math.abs(lat), 2)) + Math.pow(Math.abs(lon), 2));

        if(diff < circle.getRadius()){
            return true;
        } else{
            return false;
        }
    }
    private CircleOptions GetMarkerBounds(Marker marker){
        double meters = 0.00003;

        CircleOptions circle_opt = new CircleOptions();
        circle_opt.center(marker.getPosition());
        circle_opt.radius(meters);

        return circle_opt;
    }
    private LatLng getRandomPoint(){
        LatLng rand_point = null;

        LatLng ne = currScreen.northeast;
        LatLng sw = currScreen.southwest;

        double upper_lat = sw.latitude;
        double lower_lat = ne.latitude;
        double upper_lon = ne.longitude;
        double lower_lon = sw.longitude;

        double latitude = rand.nextDouble() * (upper_lat - lower_lat) + lower_lat;
        double longitude = rand.nextDouble() * (upper_lon - lower_lon) + lower_lon;

        rand_point = new LatLng(latitude, longitude);

        return rand_point;
    }

    public void startLocationRequest(){
        LocationServices.FusedLocationApi.requestLocationUpdates(
                myGoogleApiClient, locationRequest, this);
    }


    public void startGame(View v)
    {
        Log.v("OK", "##########VISIBILITY: " + findViewById(R.id.readyFrag).getVisibility());
        findViewById(R.id.readyFrag).setVisibility(View.INVISIBLE);
        Log.v("OK", "##########VISIBILITY: " + findViewById(R.id.readyFrag).getVisibility());
        getUsersInformationThread();
    }

    public void getUsersInformationThread()
    {
        while(run)
        {
            HashMap<Integer, User> users = UserLocations.returnList();
            for(final User u : users.values()){
                Log.v("ALC2", "user: " + u.getNumber() + " Loc: " + u.getLat());
                if(u.getLat() != 0.0){
                    if(u.getNumber() != UserLocations.getMyUser()){
                        Log.v("ALC2", "create marker for user: " + u.getNumber());
                        markopt = new MarkerOptions();
                        switch(u.getNumber())
                        {
                            case 0:
                                markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon3 ));
                                break;
                            case 1:
                                markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon1 ));
                                break;
                            case 2:
                                markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon2 ));
                                break;
                            case 3:
                                markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon4 ));
                                break;
                            default:
                                markopt.position(new LatLng(u.getLat(), u.getLon())).title(u.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon3 ));
                                break;
                        }
                        Log.v("ALC2", "Marker created for: " + u.getNumber() + " at: " + u.getLat() + ", " + u.getLon());

                        handler.post(new Runnable(){
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                addUserMarker(markopt, u.getNumber());
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
        Marker new_mark = mMap.addMarker(markopt);
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

    public void startingMapCoor(LatLngBounds in)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(in,0));
        currScreen = in;
    }


    public void startCount(boolean in)
    {
        if(in)
            tCount++;
        else if (!in)
            tCount--;
        Log.v("P2P","tCount " + tCount);
    }


    public void toggleButton(View v)
    {
        ToggleButton r = (ToggleButton)v;
        if(r.isChecked())
            letsPlay(true, r, v);
        else if (!r.isChecked())
            letsPlay(false, r, v);
    }


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = info.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            Log.v("P2P", "You are the group owner");
            Log.v("P2P", "Connection established!");
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Log.v("P2P", "Connection established!");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        receiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(receiver, intentFilter);

        //startRegistration();

        initializeDiscovery();
    }


    @Override
    public void onPause() {
        unregisterReceiver(receiver);
        mManager.removeGroup(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.v("P2P", "Group Removed");
            }

            @Override
            public void onFailure(int reason) {
                Log.v("P2P", "Group Not Removed: " + reason);
            }
        });
        super.onPause();
    }


    @Override
    public void onDestroy() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // initiate clearing of the all service requests

            }
            @Override
            public void onFailure(int i) {
                Log.d("P2P", "FAILED to stop discovery");
            }
        });

        mManager.removeGroup(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.v("P2P", "Group Removed");
            }

            @Override
            public void onFailure(int reason) {
                Log.v("P2P", "Group Not Removed: " + reason);
            }
        });
        myGoogleApiClient.disconnect();
        // Disconnect from wifi to avoid channel conflict
        //mWifiManager.disconnect();
        super.onDestroy();
    }



    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(myGoogleApiClient);
        Log.v("P2P", "location " + location);
        LatLng temp = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp,19));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(UserLocations.getMyUser() != INITIAL_PACKET_NUMBER) {
            User u = UserLocations.getUser(UserLocations.getMyUser());
            Log.v("LOC", "Setting user: " + UserLocations.getMyUser() + "'s location to: " + location);
            u.setNumber(UserLocations.getMyUser());
            u.setLat(location.getLatitude());
            u.setLon(location.getLongitude());
            UserLocations.setUser(u);
            if(deviceServiceStarted)
            {
                checkLocation(u);
            }
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("LOC", "GoogleApiClient connection has failed");
    }


    public static class UserLocations {

        static HashMap<Integer, User> userLoc = new HashMap<Integer, User>();
        static boolean there = false;
        static int myUser;

        public static HashMap<Integer, User> returnList() {

            return userLoc;
        }

        public static void setUser(User user) {

            there = false;
            if (user.getNumber() < 250) {
                userLoc.put(user.getNumber(), user);
            }

        }

        public static User getUser(int i) {
            return userLoc.get(i);
        }

        public static int getMyUser() {

            Log.v("UL", "UserLocations getMyUser: " + myUser);
            return myUser;
        }

        public static void setMyUser(int i) {
            myUser = i;
            Log.v("UL", "UserLocations setMyUser: " + myUser);
        }
    }
}