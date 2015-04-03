package ca.drsystems.unleash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

//Wifi Direct Broadcast receiver class that creates listeners and backend for wifi direct connections
public class WifiDirectBroadcastReceiver extends BroadcastReceiver{

    public Play activity;
    public WifiP2pManager manager;
    public Channel channel;
    PeerListListener peerListListener;
    //Constructor
    public WifiDirectBroadcastReceiver(WifiP2pManager mManager, Channel mChannel, Play mActivity, PeerListListener mPeerListListener) {
        this.manager = mManager;
        this.channel = mChannel;
        this.activity = mActivity;

        this.peerListListener = mPeerListListener;
    }

    //OverRide handlers for received Wifi Direct Broadcasts
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //P2P enabled switch notification
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
            }
        //Change in the peerlist of connected Peers
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.

            if(manager != null){
                manager.requestPeers(channel, peerListListener);
            }
            Log.v("P2P", "Peers Changed!");
            //activity.connect();
        //Change in this devices connected status to another device
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
        //Change is just this devices status
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
