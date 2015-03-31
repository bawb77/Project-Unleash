package ca.drsystems.unleash;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Play extends FragmentActivity implements WifiP2pManager.ConnectionInfoListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public Score scoreFrag = new Score();
    public joinFrag jf = new joinFrag();
    FragmentManager fragmentManager = getSupportFragmentManager();


    private final IntentFilter intentFilter = new IntentFilter();
    private List<WifiP2pDevice> peersAvailable = new ArrayList();
    private Map<Integer, Marker> powerUpList;
    private Map<Integer, CircleOptions> powerUpListCircle;
    private Map<Integer, Marker> userPosition;
    private boolean isWifiP2pEnabled;
    public List<Circle> circles;
    public List<Polyline> rects;

    LocationListener locationListener;

    ClientDeviceService clientDeviceService;
    public static final LocationRequest locationRequest = new LocationRequest()
            .create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500);
    GoogleApiClient myGoogleApiClient;
    WifiManager mWifiManager;
    final HashMap<String, String> buddies = new HashMap<String, String>();
    HostService hostService;
    Channel mChannel;
    Context context;
    TextView numPlayer;
    Location userLocation;
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
    uPowerUp u_PowerUp;
    uPlayerTracking u_PlayerTracking;
    private MediaPlayer mPlayer;
    private SoundPool soundPool;
    public int explosion_sound;
    private boolean sound_on;
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
    final int END_GAME = 249;


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
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
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
        uSound u_sound = new uSound(Play.this,mPlayer,soundPool);

        u_sound.startSound();
        initializeIntents();
        createPeerListListener();
        setUpMapIfNeeded();
        u_PowerUp = new uPowerUp(Play.this,mMap,powerUpList,powerUpListCircle);
        u_PlayerTracking = new uPlayerTracking(Play.this,mMap,userPosition);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        joinFragStart();
    }

    private void scoreFragStart() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map, scoreFrag).commit();

        TextView temp = (TextView)findViewById(R.id.tv_score);
    }

    public void joinFragStart() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
                if(findViewById(R.id.readyFrag).getVisibility() == View.VISIBLE) {
                    Log.v("P2P", "onPeersAvailable() call");
                    // Out with the old, in with the new.

                    peersAvailable.clear();
                    peersAvailable.addAll(peerList.getDeviceList());

                    Log.v("P2P", "peerList: " + peerList);

                    numPlayer = (TextView) findViewById(R.id.numPlayers);
                    numPlayer.setText("" + peersAvailable.size());

                    for (WifiP2pDevice device : peersAvailable) {
                        if (!deviceServiceStarted || deviceServiceStarted && host)
                            connect(device);
                    }
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
        Random rand = new Random();
        Log.v("DEVICE", "Model: " + Build.MODEL + ", DEVICE: " + Build.DEVICE);


        config.groupOwnerIntent = 15;
        config.groupOwnerIntent = rand.nextInt(14);

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

                if(reason == 2){
                    peersAvailable.clear();
                    peerListListener = null;
                    createPeerListListener();
                }
                else{
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(device1.status != 1)
                                connect(device1);
                        }
                    }, 1000);
                }
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

        if(host && deviceServiceStarted)
        {
            Log.d("P2P", "***********************HOST SENDS STRCON********************");
            Log.v("P2p", "Counts" + tCount + ":" + connected);
            if(tCount == connected) {
                Log.v("P2P", "SHOULD BE STARTING NOW FUCK");
                LatLngBounds temp = mMap.getProjection().getVisibleRegion().latLngBounds;
                currScreen = temp;
                LatLng temp2 = temp.northeast;
                LatLng temp3 = temp.southwest;
                startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser(),temp2.latitude, temp2.longitude, temp3.latitude,temp3.longitude);
                hostService.sendToAll(START_CONDITIONS, strCon);
                startGame();
                u_PowerUp.startSpawn();
            }
            else
            {
                r.setChecked(false);
            }
        }
        else if(!host && deviceServiceStarted)
        {
            Log.d("P2P", "***********************Client SENDS STRCON********************");
            startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser());
            clientDeviceService.send(START_CONDITIONS, strCon);
        }
        else{
            r.setChecked(false);
        }

    }

    public void unleash(View v){
        Log.v("UNLEASH", "UNLEASHING " + host + ", " + powerLevel);
        if(powerLevel >= 3){
            if(host)
            {
                Log.v("UNLEASH", "Host Unleashing");
                Context context = getApplicationContext();
                CharSequence text = "UNLEASH";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                showUnleashAnimation();
                UnleashAttack attack = new UnleashAttack(UserLocations.getMyUser(),powerLevel,userLocation.getLatitude(),userLocation.getLongitude());
                hostService.sendToAll(UNLEASH_C,attack);
                u_PowerUp.decreasePowerLevel();

            }else{
                UnleashAttack attack = new UnleashAttack(UserLocations.getMyUser(),powerLevel,userLocation.getLatitude(),userLocation.getLongitude());
                clientDeviceService.send(UNLEASH_C,attack);
                u_PowerUp.decreasePowerLevel();
            }
        }else{
            Context context = getApplicationContext();
            CharSequence text = "YOUR POWER ISN'T HIGH ENOUGH";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
    private void showUnleashAnimation(){

        LatLng temp = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
        Log.v("UNL", "Unleashing at " + userLocation.getLatitude() + ", " + userLocation.getLongitude());
        new AnimateUnleash(Play.this, temp, powerLevel, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.v("ASYNC", "DO IN BACKGROUND OK");
                return null;
            }
        }.execute(null, null, null);
        //hostService.explosions(temp);
        //soundPool.play(explosion_sound, 1.0f, 1.0f, 0, 0, 1.0f);
    }


    public void startLocationRequest(){
        LocationServices.FusedLocationApi.requestLocationUpdates(
                myGoogleApiClient, locationRequest, this);
    }


    public void startGame()
    {
        Log.v("OK", "##########VISIBILITY: " + findViewById(R.id.readyFrag).getVisibility());
        findViewById(R.id.readyFrag).
        findViewById(R.id.readyFrag).setVisibility(View.INVISIBLE);
        Log.v("OK", "##########VISIBILITY: " + findViewById(R.id.readyFrag).getVisibility());
        new Thread(new Runnable()
        {
            public void run(){
                u_PlayerTracking.getUsersInformationThread();
            }
        }).start();
        scoreFragStart();

    }
    public Circle addCircle(CircleOptions circle){
        Log.v("CIRCLE", "ADDING CIRCLE");
        if(host)
        {
            Log.v("CIRCLE", "I AM A HOST ADDING CIRCLE");
            HashMap<Integer, User> killUsers = UserLocations.returnList();
            for(User u : killUsers.values())
            {
                if(IsLocationInCircle(u.getLatLng(),circle) && u.getNumber() != UserLocations.getMyUser()){
                    if(host)
                    {
                        u.setAlive(false);
                        checkVictory();
                    }
                }
            }
        }
        Circle circle_tmp = mMap.addCircle(circle);
        circles.add(circle_tmp);
        return circle_tmp;
    }
    private void checkVictory()
    {
        Log.v("VICTORY", "CHECKING VICTORY");
        HashMap<Integer, User> countUsers = UserLocations.returnList();
        int aliveCount =0;
        String winner = "";
        for(User u : countUsers.values())
        {

            if(u.getAlive())
            {
                aliveCount++;
                winner += u.getName();
            }
        }
        Log.v("VICTORY", "winner: " + winner);
        if(aliveCount == 1)
        {
            EndGame finished = new EndGame(true,winner);
            hostService.sendToAll(END_GAME, finished);
            Intent intent = new Intent(this, GameOver.class);
            intent.putExtra("winner", winner);
            startActivity(intent);
        }
    }

    public boolean IsLocationInCircle(LatLng location, CircleOptions circle){
        double lat = Math.abs(location.latitude) - Math.abs(circle.getCenter().latitude);
        double lon = Math.abs(location.longitude) - Math.abs(circle.getCenter().longitude);

        double diff = Math.sqrt((Math.pow(Math.abs(lat), 2)) + Math.pow(Math.abs(lon), 2));

        if(diff < circle.getRadius()){
            return true;
        } else{
            return false;
        }
    }
    public void removeCircle(Circle circle){
        circle.remove();
    }

    public Polyline addRect(PolylineOptions poly){
        Polyline polyline = mMap.addPolyline(poly);
        return polyline;
    }

    public void startingMapCoor(LatLngBounds in)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(in, 0));
        currScreen = in;

    }

    public void startCount(boolean in)
    {
        if(in)
            tCount++;
        else if (!in)
            tCount--;
        Log.v("P2P", "tCount " + tCount);
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
        if(sound_on){
            mPlayer.start();
        }
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
    protected void onStop() {
        if(sound_on){
            mPlayer.pause();
        }
        run = false;
        // TODO Auto-generated method stub
        super.onStop();
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 19));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
        if(UserLocations.getMyUser() != INITIAL_PACKET_NUMBER) {
            User u = UserLocations.getUser(UserLocations.getMyUser());
            Log.v("LOC", "Setting user: " + UserLocations.getMyUser() + "'s location to: " + location);
            u.setNumber(UserLocations.getMyUser());
            u.setLat(location.getLatitude());
            u.setLon(location.getLongitude());
            UserLocations.setUser(u);
            if(deviceServiceStarted)
            {
                u_PowerUp.checkLocation(u);
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