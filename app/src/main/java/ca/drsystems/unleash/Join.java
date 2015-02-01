package ca.drsystems.unleash;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class Join extends ActionBarActivity implements WifiP2pManager.ConnectionInfoListener {

    private final IntentFilter intentFilter = new IntentFilter();
    private List<WifiP2pDevice> peersAvailable = new ArrayList();
    private List<WifiP2pDevice> peersConnected = new ArrayList();
    private boolean isWifiP2pEnabled;


    Channel mChannel;
    WifiP2pManager mManager;
    WifiDirectBroadcastReceiver receiver;
    PeerListListener peerListListener;
    WifiP2pInfo info;

    /**
     * Adding WifiP2pConfig object is last change.
     * We should be able to see other devices now (although we don't have a
     * fragment to display them in like last time
     *
     * Will need to move where it is created to a method that is called for every
     * peer in the list
     *
     * http://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html#connect
     * @param savedInstanceState
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        initializeIntents();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        createPeerListListener();

        connect();
    }



    @Override
    public void onResume(){
        super.onResume();

        receiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(receiver, intentFilter);
        Log.v("P2P", "WifiDirectBroadcastReceiver registered");

        initializeDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initializeIntents(){
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Log.v("P2P", "Initialized Intents");
    }

    private void initializeDiscovery() {
        mManager.discoverServices(mChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.v("P2P", "Peer Discovery Initialized");
                Toast.makeText(Join.this, "Looking for friends!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.v("P2P", "Peer Discovery Failed To Initialize");
                Toast.makeText(Join.this, "Error finding friends, code : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
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

                // If an AdapterView is backed by this data, notify it
                // of the change.  For instance, if you have a ListView of available
                // peers, trigger an update.
                /*((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
                if (peers.size() == 0) {
                    Log.d(WiFiDirectActivity.TAG, "No devices found");
                    return;
                }*/
            }
        };
    }


    public void connect() {

        mManager.requestPeers(mChannel, peerListListener);

        //Goes through each device in the ArrayList "peers" and connects to it
        if(!info.groupFormed) {

            //Creates the group if it hasn't been formed yet
            mManager.createGroup(mChannel, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.v("P2P", "Group formed successfully!");
                }

                @Override
                public void onFailure(int reason) {
                    Log.v("P2P", "Group formation failed");
                }
            });
        }

        if(info.isGroupOwner){
            for (WifiP2pDevice device : peersAvailable) {

                Log.v("P2P", "Connecting to device: " + device.deviceName +
                        " with address: " + device.deviceAddress);

                final WifiP2pDevice device1 = device;

                if(!peersConnected.contains(device1)) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device1.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;


                    mManager.connect(mChannel, config, new ActionListener() {

                        @Override
                        public void onSuccess() {
                            // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                            Log.v("P2P", "Connection to: " + device1.deviceName +
                                    "initiated");
                            peersConnected.add(device1);
                            peersAvailable.remove(device1);
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.v("P2P", "Connection to: " + device1.deviceName +
                                    "initiation failed");
                        }
                    });
                }
            }
        }
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
}
