package ca.drsystems.unleash;

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
    private PowerUp tmp_power;
    private startCondition tmp_stc;
    private UnleashAttack tmp_unleash_c;
	
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
                case START_CONDITIONS:
                    tmp_stc = (startCondition) p.getData();
                    Log.v("PORT","STCON " + tmp_stc.getReady() + ", mapSet(): " + tmp_stc.mapSet());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PlayAct.startingMapCoor(tmp_stc.mapSet());
                            if(tmp_stc.getReady())
                            {
                                Log.v("PORT","Client Start Game");
                                PlayAct.startGame(PlayAct.findViewById(R.id.readyFrag));
                            }
                        }
                    });
                    break;
                case USER_CLASS:
                    tmp_user = (User) p.getData();

                    Log.v("PORT", "Client Receiving setUserLocation for: " + tmp_user.getNumber() + ", Lat: " + tmp_user.getLat());
                    if (tmp_user.getNumber() != this.user.getNumber())
                        UserLocations.setUser(tmp_user);
                    break;
                case POWER_UP:
                    tmp_power = (PowerUp)p.getData();
                    if(tmp_power.status){
                        PlayAct.u_PowerUp.removePowerUp(tmp_power.getPowerNum());
                        if(tmp_power.getPlayer() == UserLocations.getMyUser())
                        {
                            PlayAct.u_PowerUp.increasePowerLevel();
                        }
                    }
                    else{
                        PlayAct.u_PowerUp.makePowerUp(tmp_power.getPowerNum(),tmp_power.getLatLng());
                    }

                    break;
                case UNLEASH_D:
                    //release unleash direction blast
                    break;
                case UNLEASH_C:
                    tmp_unleash_c = (UnleashAttack)p.getData();
                    new AnimateUnleash(PlayAct,tmp_unleash_c.getLocation(), PlayAct,tmp_unleash_c.getPowerLvl(), true).execute();
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

	
	private void SendThread(){
		while(true){
            if(user.getNumber() != INITIAL_PACKET_NUMBER){
                handler.post(new Runnable(){
                     @Override
                     public void run(){
					 user = UserLocations.getUser(UserLocations.getMyUser());
					 if(user != null){
						 Log.v("PORT", "Sending: " + user);
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
			try {
				//ObjectInputStream ois = new ObjectInputStream(is);
				if(ois.available() == 0){
					Object o;
					o = ois.readObject();

					UnleashPackage p = (UnleashPackage)o;
                    Log.v("PORT", "Object received from ois: " + p.getHeader() +  " data " + p.getData());
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
		UnleashPackage p = new UnleashPackage(header, o);
		
		try {
			Log.v("PORT", "Sending my packet: " + header + " With data: " + p.getData());
			oos.writeObject(p);
			Log.v("PORT", "Sent my packet: " + header);
		} catch (IOException e) {
			Log.v("PORT", "send method: IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
