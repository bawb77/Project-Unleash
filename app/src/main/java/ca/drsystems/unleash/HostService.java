package ca.drsystems.unleash;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HostService extends AsyncTask<Void, Void, String>{
	//Global Variables
	public Handler handler;
    public final static int PORT = 12345;
    public ServerSocket server;
    public Play PlayAct;
    public WifiP2pDevice device;
    public Socket client;
    public OutputStream os;
    public InputStream is;
    //Class Variables
    private List<ClientService> clientServiceList;
	private int user_num;
	private boolean run;
    //Header Codes
    private final int INITIAL_PACKET_NUMBER = 255;
    private final int START_CONDITIONS = 254;
    private final int USER_CLASS = 253;
    private final int POWER_UP = 252;
    private final int UNLEASH_C = 251;
    private final int UNLEASH_D = 250;
	//Constructor
	public HostService(Handler h, Play a){
		this.handler = h;
		this.PlayAct = a;
		this.run = false;
		createSockets();
        //set up host player info in the static class
        User u = new User();
        u.setNumber(0);
        u.setAlive(true);
        Play.UserLocations.setMyUser(0);
        Play.UserLocations.setUser(u);
        //ArrayList for keeping Track of clients
        clientServiceList = new ArrayList<ClientService>();
	}
	
	private void createSockets(){
		// create Socket for Server to accept Sockets from the Clients
		try {
			Log.v("SOCK", "Try to create ServerSocket on port: " + PORT);
			server = new ServerSocket(PORT);
			Log.v("SOCK", "ServerSocket creation successful.");
			run = true;
		} catch (IOException e) {
			Log.v("SOCK", "IOException HostService.java: in createSockets()");
			e.printStackTrace();
		}
	}
    //Method for using general Send method in all ClientService Instances at the same time
    public void sendToAll(int header, Object o)
    {
        Log.v("P2P", "Sending header: " + header + " to all");
        for(ClientService iter : clientServiceList)
        {
            Log.v("P2P", "SendToAll Sending to " + iter);
            iter.send(header, o);
        }
    }
    //Host Logic for Power up collection. If two players pick up power up at the same time the one with the first time stamp gets the power up.
    public void decider(PowerUp in){
        powerUpDecider.storedPowerUpList.add(in);
        long king = 999999999;
        PowerUp winner =  in;
        for(PowerUp temp : powerUpDecider.storedPowerUpList)
        {
            if (temp.getTime()<king) {
                king = temp.getTime();
                winner = temp;
            }
        }
        PlayAct.u_PowerUp.removePowerUp(winner.getPowerNum());
        powerUpDecider.storedPowerUpList.clear();
        sendToAll(POWER_UP,winner);
    }
    //static class for holding powerups
    public static class powerUpDecider
    {
        static List<PowerUp> storedPowerUpList = new LinkedList<>();
    }

	@Override
	protected String doInBackground(Void... params) {
		user_num = 1;
		//waiting for socket connections
		while(!run){
			Log.v("SOCK", "ServerSocket not created yet");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//once socket created accept the connection and create input and output streams.
		while(run){
			try {
				Log.v("SOCK", "While run==TRUE, wait for client: " + user_num + "'s socket to connect");
				client = server.accept();
				Log.v("SOCK", "We got a client! client's InetAddress: " + client.getInetAddress());
				os = client.getOutputStream();
				is = client.getInputStream();
                //if sockets and streams are created successfully, then create a dedicated ClientService to handle that particular Client and add it to the Arraylist for use with the send all method
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	Log.v("SOCK", "Creating new Thread for client: " + user_num);
				        	ClientService temp = new ClientService(handler, PlayAct, device, user_num, client, os, is);
                            clientServiceList.add(temp);
                            //increment for game start logic
                            PlayAct.connected++;
                            //increment so next ClientService created is new player number
				        	user_num++;
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				});
				
				thread.start();
				
			} catch (IOException e) {
                Log.v("SOCK", "IOException creating IO streams for client " + user_num);
				e.printStackTrace();
			}
		}		
		return null;
	}
}
