package ca.drsystems.unleash;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import ca.drsystems.unleash.Play.UserLocations;

public class ClientDeviceService extends AsyncTask<Void, Void, String>{
    //global variables
	public Socket client;
	public Handler handler;
	public Play PlayAct;
    public User user;
    public WifiP2pInfo info;
    public OutputStream os;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public InputStream is;
    //class variables
	private int PORT;
    private User tmp_user;
    private PowerUp tmp_power;
    private startCondition tmp_stc;
    private UnleashAttack tmp_unleash_c;
	private InetAddress server;
	private boolean socket_done;
	//header codes
    final int INITIAL_PACKET_NUMBER = 255;
    final int START_CONDITIONS = 254;
    final int USER_CLASS = 253;
    final int POWER_UP = 252;
    final int UNLEASH_C = 251;
    final int UNLEASH_D = 250;
    final int END_GAME = 249;

	//constructor
	public ClientDeviceService(Handler handler, Play a, int port, InetAddress s){
		this.handler = handler;
		this.PlayAct = a;
		this.PORT = port;
		this.socket_done = false;
		this.server = s;
		this.user = new User();
		this.user.setNumber(INITIAL_PACKET_NUMBER);
		Log.v("ALC", "in ClientDeviceService");
		createSocketThread();
	}
    //create socket to connect to host device.
	private void createSocketThread()
	{
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	createSocket();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		});
		
		thread.start();
	}
	
	private void createSocket(){
		Log.v("ALC", "in createSocket");
        //while loop on tripwire Boolean
		while(!socket_done){
		try {
			Log.v("PORT", "creating server socket in client: " + server);
            //client creates a new socket based on the Wifi direct host ip address with standard 12345 port
			client = new Socket(server, PORT);
			Log.v("PORT", "server socket created!");
            //create input and output streams with new socket connects
			os = client.getOutputStream();
			oos = new ObjectOutputStream(os);
			is = client.getInputStream();
			ois = new ObjectInputStream(is);
            //tripWire boolean set
			socket_done = true;
			Log.v("PORT", "socket creation done with oos and ois");
            //error handling
		} catch (UnknownHostException e) {
			Log.v("PORT", "UnknownHostException from new Socket(server)");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("PORT", "IOException from new Socket(server)");
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	}
	//Handler method for all incoming packets. Switch case on header code determines behaviour
	private void ReceiveThread() {
        while (true) {
            Log.v("PORT", "in receiveThread");
            // call out to actual receive thread
            UnleashPackage p = receive();
            Log.v("PORT", "received package from host");
            //strip out header code from package
            int header = p.getHeader();
            switch (header) {
                //first received package from host will have this header. this package gives the client device it's player number for this game instance
                case INITIAL_PACKET_NUMBER:
                    User u = (User) p.getData();
                    Log.v("PORT", "Initial: " + p.getData());

                    this.user.setNumber(u.getNumber());
                    this.user.setName(u.getName());
                    this.user.setLat(u.getLat());
                    this.user.setLon(u.getLon());
                    this.user.setAlive(true);
                    UserLocations.setMyUser(user.getNumber());
                    UserLocations.setUser(u);
                    Log.v("PORT", "Set my user to: " + u);
                    break;
                //this object is received when the user's press the ready button on the join screen and the sockets have been set up.
                //It gives the screen position and oreintation of the host so all players are playing on the same field.
                case START_CONDITIONS:
                    tmp_stc = (startCondition) p.getData();
                    Log.v("PORT","STCON " + tmp_stc.getReady() + ", mapSet(): " + tmp_stc.mapSet());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PlayAct.startingMapCoor(tmp_stc.mapSet());
                            //if received object has true for it's ready state start the game
                            if(tmp_stc.getReady())
                            {
                                Log.v("PORT","Client Start Game");
                                PlayAct.startGame();
                            }
                        }
                    });
                    break;
                //standard User Object for player tracking. Clients receive a separate package for each player in the game from the host.
                case USER_CLASS:
                    tmp_user = (User) p.getData();

                    Log.v("PORT", "Client Receiving setUserLocation for: " + tmp_user.getNumber() + ", Lat: " + tmp_user.getLat());
                    if (tmp_user.getNumber() != this.user.getNumber())
                        UserLocations.setUser(tmp_user);
                    break;
                //when the host creates a power up marker it is distributed to all players and maintained so only one player can pick up each power up.
                case POWER_UP:
                    tmp_power = (PowerUp)p.getData();
                    //if true then it means the host has assigned the powerup to a player and it needs to be removed from main thread
                    if(tmp_power.getStatus()){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PlayAct.u_PowerUp.removePowerUp(tmp_power.getPowerNum());
                                if (tmp_power.getPlayer() == UserLocations.getMyUser()) {
                                    PlayAct.u_PowerUp.increasePowerLevel();
                                }
                            }
                        });
                    }
                    //if false it means this is a new power up that needs to be placed on the field
                    else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PlayAct.u_PowerUp.makePowerUp(tmp_power.getPowerNum(), tmp_power.getLatLng());
                            }
                        });
                    }

                    break;
                //to be completed
                case UNLEASH_D:
                    //release unleash direction blast
                    break;
                //When the client unleashes the command is first sent to the host and then relayed back to the client for animation as the kill logic is dealt with by the host.
                case UNLEASH_C:
                    tmp_unleash_c = (UnleashAttack)p.getData();
                    new AnimateUnleash(PlayAct,tmp_unleash_c.getLocation(),tmp_unleash_c.getPowerLvl(), true).execute();
                    PlayAct.u_sound.explosions();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Log.v("ASYNC", "DO IN BACKGROUND OK");
                            return null;
                        }
                    }.execute(null, null, null);
                    break;
                //Object is sent out by the host when all other players are removed from the game.
                case END_GAME:
                    EndGame temp = (EndGame)p.getData();
                    //win screeen
                    Intent intent = new Intent(PlayAct, GameOver.class);
                    intent.putExtra("winner", temp.getPlayer());
                    PlayAct.startActivity(intent);
                    break;
                default:
                    break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

	// thread for continuously updating host with user position information
	private void SendThread(){
		while(true){
            if(user.getNumber() != INITIAL_PACKET_NUMBER){
                handler.post(new Runnable(){
                     @Override
                     public void run(){
                         //pull user location out of static class
					 user = UserLocations.getUser(UserLocations.getMyUser());
					 if(user != null){
						 Log.v("PORT", "Sending: " + user);
                         //send out user data
						 send(USER_CLASS,user);
						 Log.v("PORT", "Sent: " + user);
					 }
				 }
			 });
		}
		 try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	//doInBackground method starts up receive and send threads.
	@Override
	protected String doInBackground(Void... arg0) {
		
		while(!socket_done){
			try {
				Log.v("PORT", "socket not built yet");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Log.v("PORT", "start ReceiveThread");
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	ReceiveThread();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		});
		
		thread.start();
		
		Log.v("PORT", "start SendThread");
		Thread thread2 = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	SendThread();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		});
		
		thread2.start();
		 
		return null;
	}
	//Receive method called by receiveThread
	public UnleashPackage receive(){
			try {
				//Check if input stream has any waiting data
				if(ois.available() == 0){
					Object o;
					o = ois.readObject();
                    Log.v("OK", "" + o);
					UnleashPackage p = (UnleashPackage)o;
                    Log.v("PORT", "Object received from ois: " + p.getHeader() +  " data " + p.getData());
                    Log.v("TEST", "Does object equal casted object? " + o.equals(p));
					return p;
				}
                //error handling
			} catch (ClassNotFoundException e) {
				Log.v("PORT", "ois.readObject: ClassNotFoundException");
				e.printStackTrace();
			} catch (OptionalDataException e) {
				Log.v("PORT", "ois.readObject: OptionalDataException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.v("PORT", "ois.readObject: IOException");
				e.printStackTrace();
			}
		//default return
		return null;
	}
	//general send thread used by sendThread and send calls from play.
	public void send(int header, Object o){
        //package object with header code to create serializable object for sending to host.
		UnleashPackage p = new UnleashPackage(header, o);
        Log.v("OK", "Object o's toString(): " + o);
		
		try {
			Log.v("PORT", "Sending my packet: " + header + " With data: " + p.getData());
			oos.writeObject(p);
			Log.v("PORT", "Sent my packet: " + header + " With data: " + p.getData());
            //error handling
		} catch (IOException e) {
			Log.v("PORT", "send method: IOException");
			e.printStackTrace();
		}
	}

}
