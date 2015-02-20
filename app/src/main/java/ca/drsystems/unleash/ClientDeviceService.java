package ca.drsystems.unleash;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.LocationServices;

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
	public Socket client;
	public Handler handler;
	public Play PlayAct;
	private int PORT;
	public User user;
	private InetAddress server;
	private boolean socket_done;
	public WifiP2pInfo info;
	public OutputStream os;
	public ObjectOutputStream oos;
	public ObjectInputStream ois;
	public InputStream is;
    final int INITIAL_PACKET_NUMBER = 255;
    final int START_CONDITIONS = 254;
    final int USER_CLASS = 253;
    final int POWER_UP = 252;
    final int UNLEASH_C = 251;
    final int UNLEASH_D = 250;
	private User tmp_user;
	
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
		while(!socket_done){
		try {
			Log.v("PORT", "creating server socket in client: " + server);
			client = new Socket(server, PORT);
			Log.v("PORT", "server socket created!");
			os = client.getOutputStream();
			oos = new ObjectOutputStream(os);
			is = client.getInputStream();
			ois = new ObjectInputStream(is);
			socket_done = true;
			Log.v("PORT", "socket creation done with oos and ois");
		} catch (UnknownHostException e) {
			Log.v("PORT", "UnknownHostException from new Socket(server)");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("PORT", "IOException from new Socket(server)");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	
	private void ReceiveThread() {
        while (true) {
            Log.v("PORT", "in receiveThread");
            UnleashPackage p = receive();
            Log.v("PORT", "received package from host");

            int header = p.getHeader();
            switch (header) {
                case INITIAL_PACKET_NUMBER:
                    Log.v("PORT", "package header = INITIAL_PACKET_NUMBER --> my own user data");
                    User u = (User) p.getData();
                    Log.v("PORT", p.getData().toString());

                    this.user.setNumber(u.getNumber());
                    this.user.setName(u.getName());
                    this.user.setLat(u.getLat());
                    this.user.setLon(u.getLon());
                    UserLocations.setMyUser(user.getNumber());
                    UserLocations.setUser(u);
                    Log.v("PORT", "Set my user to: " + u);
                    PlayAct.startLocationRequest();

                    break;
                case START_CONDITIONS:
                    startCondition sc = (startCondition) p.getData();
                    PlayAct.startingMapCoor(sc.mapSet());
                    if(sc.getNumber()==0 && sc.getReady())
                        PlayAct.startGame();
                    break;
                case USER_CLASS:
                    Log.v("PORT", "package header = User class");
                    tmp_user = (User) p.getData();

                    Log.v("PORT", "setUserLocation for: " + tmp_user.getNumber() + ", Lat: " + tmp_user.getLat());
                    if (tmp_user.getNumber() != this.user.getNumber())
                        UserLocations.setUser(tmp_user);
                    break;
                case POWER_UP:
                    //place powerup on map
                    break;
                case UNLEASH_D:
                    //release unleash direction blast
                    break;
                case UNLEASH_C:
                    //release unleash circular blast.
                    break;
                default:
                    break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

	
	private void SendThread(){
		while(true){
		Log.v("PORT", "in sendThread");
			
		if(user.getNumber() != INITIAL_PACKET_NUMBER){
			handler.post(new Runnable(){
				 @Override
				 public void run(){
					 Log.v("PORT", "get my user for sending");
					 user = UserLocations.getUser(UserLocations.getMyUser());
					 if(user != null){
						 Log.v("PORT", "my user lat: " + user.getLat());
						 send(USER_CLASS, user);
						 Log.v("PORT", "my user data sent");
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
	
	public UnleashPackage receive(){
		Log.v("PORT", "in receive");
		
			try {
				//ObjectInputStream ois = new ObjectInputStream(is);
				if(ois.available() == 0){
					Log.v("PORT", "ois is available");
					Object o;
					o = ois.readObject();
					Log.v("PORT", "object received vom ois");
					UnleashPackage p = (UnleashPackage)o;
					return p;
				}
			} catch (ClassNotFoundException e) {
				Log.v("PORT", "ois.readObject: ClassNotFoundException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OptionalDataException e) {
				Log.v("PORT", "ois.readObject: OptionalDataException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				Log.v("PORT", "ois.readObject: IOException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return null;
	}
	
	public void send(int header, Object o){
		Log.v("PORT", "in send");
		UnleashPackage p = new UnleashPackage(header, o);
		
		try {
			//ObjectOutputStream oos = new ObjectOutputStream(os);
			Log.v("PORT", "try sending my info: (lat: " + this.user.getLat() + ")");
			oos.writeObject(p);
			Log.v("PORT", "send method: sent");
		} catch (IOException e) {
			Log.v("PORT", "send method: IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
