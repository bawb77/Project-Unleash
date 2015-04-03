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
//Main Class of the game. Handles the WifiDirect Connect and then starts up Services to handle the Information Flow
//Also contains the majority of the game logic and run processes.
public class Play extends FragmentActivity implements WifiP2pManager.ConnectionInfoListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    //global variables
    public static final LocationRequest locationRequest = new LocationRequest()
            .create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500);
    public GoogleApiClient myGoogleApiClient;
    protected ClientDeviceService clientDeviceService;
    protected WifiManager mWifiManager;
    protected final HashMap<String, String> buddies = new HashMap<String, String>();
    protected HostService hostService;
    protected Channel mChannel;
    protected Context context;
    protected TextView numPlayer;
    protected Location userLocation;
    protected WifiP2pManager mManager;
    protected WifiDirectBroadcastReceiver receiver;
    protected PeerListListener peerListListener;
    protected WifiP2pServiceInfo serviceInfo;
    protected WifiP2pDnsSdServiceRequest serviceRequest;
    protected WifiP2pInfo gInfo;
    protected MarkerOptions markopt;
    protected Handler handler;
    protected LatLngBounds currScreen;
    protected Random rand;
    protected uPowerUp u_PowerUp;
    protected uPlayerTracking u_PlayerTracking;
    protected uSound u_sound;
    protected MediaPlayer mPlayer;
    protected SoundPool soundPool;
    protected boolean sound_on;
    protected boolean deviceServiceStarted;
    protected boolean host;
    protected boolean run;
    protected int powerLevel;
    protected int connected;
    protected int tCount;
    protected int pCount;
    //create powerlevel display frag
    protected Score scoreFrag = new Score();
    //create waiting lobby frag
    protected joinFrag jf = new joinFrag();
    protected List<Circle> circles;
    protected List<Polyline> rects;
    //class variables
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private final IntentFilter intentFilter = new IntentFilter();
    private List<WifiP2pDevice> peersAvailable = new ArrayList();
    private Map<Integer, Marker> powerUpList;
    private Map<Integer, CircleOptions> powerUpListCircle;
    private Map<Integer, Marker> userPosition;
    private boolean isWifiP2pEnabled;
    //header codes
    private final int INITIAL_PACKET_NUMBER = 255;
    private final int START_CONDITIONS = 254;
    private final int USER_CLASS = 253;
    private final int POWER_UP = 252;
    private final int UNLEASH_C = 251;
    private final int UNLEASH_D = 250;
    private final int END_GAME = 249;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        //set user to default package header to ready to receive player number from host
        UserLocations.setMyUser(INITIAL_PACKET_NUMBER);
        //start up google API
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        myGoogleApiClient.connect();
        //Holders for Power up logic
        powerUpList = new HashMap<Integer, Marker>();
        powerUpListCircle = new HashMap<Integer, CircleOptions>();
        userPosition = new HashMap<Integer, Marker>();
        //Connection Manager
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        //random for power up
        rand = new Random();
        pCount = 0;
        //player powerlevel
        powerLevel = 3;
        //trip wire booleans
        host = false;
        run = true;
        deviceServiceStarted = false;
        //handler for passing to services
        handler = new Handler();
        //context
        context = this;
        //managers for wifi direct connect
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        //methodized sound package instantiation
        u_sound = new uSound(Play.this,mPlayer,soundPool);
        u_sound.startSound();
        //Start up game Connection Logic engines
        initializeIntents();
        createPeerListListener();
        //set up Maps boundries and other information
        setUpMapIfNeeded();
        //Start up methodized power up creation
        u_PowerUp = new uPowerUp(Play.this,mMap,powerUpList,powerUpListCircle);
        //start up methodized player tracking class. actual player tracking starts after start game is called
        u_PlayerTracking = new uPlayerTracking(Play.this,mMap,userPosition);
        //inflate the join lobby frag
        joinFragStart();
    }
    //create Power level display frag for the map activity
    private void scoreFragStart() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map, scoreFrag).commit();

        TextView temp = (TextView)findViewById(R.id.tv_score);
    }
    //inflate the join lobby fragment on top of the map activity
    public void joinFragStart() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map, jf).commit();
    }
    //register intent filters see WifiDirectBroadcastReceiver
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

    //Listens for any peers available for connection over wifi direct
    private void createPeerListListener() {
        Log.v("P2P", "Creating PeerListListener");
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                if(findViewById(R.id.readyFrag).getVisibility() == View.VISIBLE) {
                    Log.v("P2P", "onPeersAvailable() call");
                    // Out with the old, in with the new.
                    //maintain list of available peers
                    peersAvailable.clear();
                    peersAvailable.addAll(peerList.getDeviceList());

                    Log.v("P2P", "peerList: " + peerList);
                    //display number of available players
                    numPlayer = (TextView) findViewById(R.id.numPlayers);
                    numPlayer.setText("" + peersAvailable.size());
                    //Connect to each device in the peerlist
                    for (WifiP2pDevice device : peersAvailable) {
                        //only attempt the connection if no connection has yet been made OR if device is host and has connected to at least one device already
                        if (!deviceServiceStarted || deviceServiceStarted && host)
                            connect(device);
                    }
                }
            }
        };
    }
    //connection protocol between two devices
    public void connect(WifiP2pDevice device) {
        //transfer to avoid concurrency issues
        final WifiP2pDevice device1 = device;
        //check if passed device is available for connections
        if (device1.status == WifiP2pDevice.AVAILABLE) {
            Log.v("P2P", "Connecting to device: " + device.deviceName +
                    " with address: " + device.deviceAddress);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device1.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            //randomize the GOI to avoid match up collisions
            Random rand = new Random();
            config.groupOwnerIntent = rand.nextInt(14);
            //actual connect call
            mManager.connect(mChannel, config, new ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Log.v("P2P", "Connection to: " + device1.deviceName +
                            " initiated");
                    //if connection attempt is successful start groupLogic to decide Host/Client
                    createGroupLogic();
                }
                @Override
                public void onFailure(int reason) {
                    Log.v("P2P", "Connection to: " + device1.deviceName +
                            " initiation failed: " + reason);
                    //if connection attempt unsuccessful with error code 2 clear the peer lists, null the listener and restart it
                    if(reason == 2){
                        peersAvailable.clear();
                        peerListListener = null;
                        createPeerListListener();
                    }
                    //else call connect again recursively until the connect is successful
                    else{
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //as long as device status isn't awaiting connection acceptance
                                if (device1.status != 1)
                                    connect(device1);
                            }
                        }, 1000);
                    }
                }
            });

        }
    }

    //decider for host/client
    public void createGroupLogic() {
        Log.v("P2P", "CreateGroupLogic");
        //pull wifi direct connection information
        mManager.requestConnectionInfo(mChannel,
            new WifiP2pManager.ConnectionInfoListener() {

                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    //check if devices have formed a group(can take a few seconds for the connection to be accepted from back in connect)
                    if (info.groupFormed) {
                        //if device is the group owner and hasn't started either the host service or the client service yet
                        if (info.isGroupOwner && !deviceServiceStarted) {
                            startHostService();
                        //if not the host and have not started the clientDeviceService yet
                        } else if (!deviceServiceStarted) {
                            startClientDeviceService(info);
                        }
                    }
                    //if no group formed yet, keep recursively calling groupLogic
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

    //starts the host service and marks this device as the host
    private void startHostService(){
        //tripwire booleans
        deviceServiceStarted = true;
        host = true;
        //set up host in static class
        UserLocations.setMyUser(0);
        //set count variables
        tCount = 0;
        connected = 0;
        Log.v("SOCK", "Starting HostService");
        //start host service
        hostService = new HostService(handler, this);
        hostService.execute();
        //start getting host user location data
        startLocationRequest();
    }

    //Client Services, Each client only starts one service
    private void startClientDeviceService(final WifiP2pInfo info){
        //tripwire boolean
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
        //start getting client user location data
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
        //disable map interfaces to prevent players from moving off of the play area
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    //???????????????????????????????????????????????????????
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
    //join ready toggle button
    public void toggleButton(View v)
    {
        ToggleButton r = (ToggleButton)v;
        if(r.isChecked())
            letsPlay(true, r);
        else if (!r.isChecked())
            letsPlay(false, r);
    }
    //logic for toggleButton call
    public void letsPlay(boolean readyTemp, ToggleButton r) {

        if(host && deviceServiceStarted && tCount == connected)
        {
            //if device is host and has started host service and other players have already pressed the ready button then do following
            Log.d("P2P", "***********************HOST SENDS STRCON********************");
            Log.v("P2p", "Counts" + tCount + ":" + connected);
            //get current screen
            LatLngBounds temp = mMap.getProjection().getVisibleRegion().latLngBounds;
            //store current screen
            currScreen = temp;
            LatLng temp2 = temp.northeast;
            LatLng temp3 = temp.southwest;
            //send current screen to all players
            startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser(),temp2.latitude, temp2.longitude, temp3.latitude,temp3.longitude);
            hostService.sendToAll(START_CONDITIONS, strCon);
            //start game and all game logic for host
            startGame();
            //start power up spawning
            u_PowerUp.startSpawn();
        }
        //for clients send a true Str_con object to the host to indicate readiness
        else if(!host && deviceServiceStarted)
        {
            Log.d("P2P", "***********************Client SENDS STRCON********************");
            startCondition strCon = new startCondition(readyTemp, UserLocations.getMyUser());
            clientDeviceService.send(START_CONDITIONS, strCon);
        }
        //keep toggleButton false if conditions have not been met
        else{
            r.setChecked(false);
        }
    }
    //unleash button method
    public void unleash(View v){
        Log.v("UNLEASH", "UNLEASHING " + host + ", " + powerLevel);
        //unleash logic
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
        //not enough power message
        }else{
            Context context = getApplicationContext();
            CharSequence text = "YOUR POWER ISN'T HIGH ENOUGH";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
    //create animation for the unleash attack
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
        u_sound.explosions();
    }

    //starts up google locations services
    public void startLocationRequest(){
        LocationServices.FusedLocationApi.requestLocationUpdates(
                myGoogleApiClient, locationRequest, this);
    }

    //start game method called by the result of the host toggle button checks
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
    //add circle method used by animnate unleash. Also used to elimanate players caught in the blast
    public Circle addCircle(CircleOptions circle){
        Log.v("CIRCLE", "ADDING CIRCLE");
        if(host)
        {
            //only host checks for kill conditions
            Log.v("CIRCLE", "I AM A HOST ADDING CIRCLE");
            HashMap<Integer, User> killUsers = UserLocations.returnList();
            for(User u : killUsers.values())
            {
                if(IsLocationInCircle(u.getLatLng(),circle) && u.getNumber() != UserLocations.getMyUser()){
                    if(host)
                    {
                        u.setAlive(false);
                        //everytime a player is elimanated check the victory conditions
                        checkVictory();
                    }
                }
            }
        }
        Circle circle_tmp = mMap.addCircle(circle);
        circles.add(circle_tmp);
        return circle_tmp;
    }

    //win condition check
    private void checkVictory()
    {
        Log.v("VICTORY", "CHECKING VICTORY");
        HashMap<Integer, User> countUsers = UserLocations.returnList();
        int aliveCount =0;
        String winner = "";
        //collect count of users still alive
        for(User u : countUsers.values())
        {

            if(u.getAlive())
            {
                aliveCount++;
                winner += u.getName();
            }
        }
        Log.v("VICTORY", "winner: " + winner);
        //if only one player remains end the game
        if(aliveCount == 1)
        {
            EndGame finished = new EndGame(true,winner);
            hostService.sendToAll(END_GAME, finished);
            Intent intent = new Intent(this, GameOver.class);
            intent.putExtra("winner", winner);
            startActivity(intent);
        }
    }
    //general use method to check if a LATLNG point is within a circle of another point.
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
    //remove circles after use
    public void removeCircle(Circle circle){
        circle.remove();
    }
    //display method for directional unleash yet to be implemented
    public Polyline addRect(PolylineOptions poly){
        Polyline polyline = mMap.addPolyline(poly);
        return polyline;
    }
    //set the global variable currScreen and move the screen to centre on the location passed
    public void startingMapCoor(LatLngBounds in)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(in, 0));
        currScreen = in;

    }
    //count logic for ready game join frag toggle button calls from clients(this method host only)
    public void startCount(boolean in) {
        if (in)
            tCount++;
        else if (!in)
            tCount--;
        Log.v("P2P", "tCount " + tCount);
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
    //google map API method OverRidden to allow us to update the static class with user location data
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
            //on every location change check user for power up possibility
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

    //static class holder for user locations. is updated both by location services form the google map api as well as passed user objects from other devices
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