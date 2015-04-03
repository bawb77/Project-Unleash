package ca.drsystems.unleash;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.HashMap;
import ca.drsystems.unleash.Play.UserLocations;

public class ClientService extends AsyncTask<Void, Void, String> {
    //global variables
    public Socket client;
    public Handler handler;
    public Play PlayAct;
    public WifiP2pDevice device;
    public InputStream is;
    public OutputStream os;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    //class variables
    private User user;
    private boolean run;
    private HashMap<Integer, User> userlist;
    private User tmp_user;
    private PowerUp tmp_power;
    private startCondition tmp_stc;
    private UnleashAttack tmp_unleash_c;
    //package headers
    private final int INITIAL_PACKET_NUMBER = 255;
    private final int START_CONDITIONS = 254;
    private final int USER_CLASS = 253;
    private final int POWER_UP = 252;
    private final int UNLEASH_C = 251;
    private final int UNLEASH_D = 250;


    //constructor; ClientService is called from the HostSerivce Thread as a Response to a Client creating a Socket Connection
    public ClientService(Handler handler, Play a, WifiP2pDevice d, int user_num, Socket c, OutputStream os, InputStream is){
        this.handler = handler;
        this.PlayAct = a;
        this.device = d;
        this.client = c;
        this.run = false;
        this.user = new User();
        this.user.setNumber(user_num);
        this.userlist = new HashMap<Integer, User>();
        this.is = is;
        this.os = os;

        Log.v("SOCKC", "NEW CLIENT| Client Number: " + user.getNumber());
        try {
            ois = new ObjectInputStream(is);
            Log.v("SOCKC", "Client " + user.getNumber() + " constructor: OIS");
        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            oos = new ObjectOutputStream(os);
            Log.v("SOCKC", "Client " + user.getNumber() + " constructor: OOS");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sendUserInfoFirst();
        startThreads();
    }
    //give Client their Player Number for this Instance of the game
    private void sendUserInfoFirst(){
        Log.v("SOCKC", "Client " + user.getNumber() + " Sending initial packet");
        send(INITIAL_PACKET_NUMBER, user);
        run = true;
    }
    //Handler method for all incoming packets. Switch case on header code determines behaviour
    private void ReceiveThread(){
        while(true){
            Log.v("SOCKC", "Client " + user.getNumber() + " Calling receive()");
            UnleashPackage p = receive();

                int header = p.getHeader();
                switch (header)
                {
                    //after the client Recieved their first package from sendUserInfoFirst the client response with their location data and confirms it's player number with Host
                    case INITIAL_PACKET_NUMBER:
                        Log.v("SOCKC", "ClientService.java: Received UnleashPackage with header = INITIAL_PACKET_NUMBER");
                        User u = (User)p.getData();

                        Log.v("SOCKC", "ClientService.java: Received UnleashPackage with user number = " + u.getNumber() + " and lat: " + u.getLat());

                        this.user.setName(u.getName());
                        this.user.setLat(u.getLat());
                        this.user.setLon(u.getLon());

                        Log.v("SOCKC", "ClientService.java: Setting user: " + user.getNumber() + " into UserLocations with: " + user.getLat());
                        UserLocations.setUser(user);
                        break;
                    //Host Receives a Ready Confirmation from the Client when the are ready to play
                    case START_CONDITIONS:
                        tmp_stc = (startCondition) p.getData();
                        Log.v("PORT","STRCON " + tmp_stc.getReady());
                        PlayAct.startCount(tmp_stc.getReady());
                        break;
                    //Host Receives User location Data from the Client and updates its static class
                    case USER_CLASS:
                        tmp_user = (User)p.getData();

                        UserLocations.setUser(tmp_user);
                        Log.v("LOC", "Host Receiving user info: " + p.getData() + " holder " + tmp_user);
                        break;
                    //Host Receives notification that player has walked within the boundries of a powerup.
                    //Host then sends player info up to HostService Decider method to handle multiple claims if they happen.
                    //therefore only one player gets the power up.
                    case POWER_UP:
                        tmp_power = (PowerUp)p.getData();
                        PlayAct.hostService.decider(tmp_power);
                        break;
                    //TBC
                    case UNLEASH_D:
                        break;
                    //Host Receives an Unleash call from one of the Clients and immediately relays it to all players as well as aninmating it for the host.
                    //the animate call has kill logic on the host device
                    case UNLEASH_C:
                        tmp_unleash_c = (UnleashAttack)p.getData();
                        new AnimateUnleash(PlayAct,tmp_unleash_c.getLocation(),tmp_unleash_c.getPowerLvl(), true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        PlayAct.hostService.sendToAll(UNLEASH_C,tmp_unleash_c);
                        break;
                    default :
                        break;
                }
                //error handling
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    //send Thread constantly updates the Client with all other player locations including the host
    private void SendThread(){
        while(true){
            handler.post(new Runnable(){
                @Override
                public void run(){
                    userlist = UserLocations.returnList();
                    Log.v("SOCKC", "Userlist: " + userlist);
                    //host sends all player data even for player owner of the socket; players ignore their own incoming data.
                    for(User u : userlist.values()){
                        User temp = new User(u);
                        send(USER_CLASS, temp);
                        Log.v("SOCKC", "User " + u.getNumber() + "'s info sent with info: " + temp.getLat());
                    }
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    //start up sending and receiving threads
    private void startThreads(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Log.v("SOCKC", "Started ReceiveThread for client: " + user.getNumber());
                    ReceiveThread();
                } catch (Exception e) {
                    Log.v("SOCKC", "Client " + user.getNumber() +
                            " Exception starting ReceiveThread");
                    e.printStackTrace();
                }
            }
        });

        thread.start();


        Thread thread2 = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Log.v("SOCKC", "Started SendThread for client: " + user.getNumber());
                    SendThread();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread2.start();
    }
    //not used in this service
    @Override
    protected String doInBackground(Void... arg0) {


        return null;
    }
    //general receive method
    public UnleashPackage receive(){
        try {
            //if stream has data waiting
            if(ois.available() == 0){
                Object o;
                o = ois.readObject();
                Log.v("OK", "Object o's toString(): " + o);
                UnleashPackage p = (UnleashPackage)o;
                Log.v("SOCKC", "Client " + user.getNumber() + " Received object" + p.getHeader() + " with data " + p.getData());
                Log.v("TEST", "Does object equal casted object? " + o.equals(p));
                return p;
            }
            //error handling
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //General send method to specific Client this ClientService was created to service
    public void send(int header_code, Object o){
        UnleashPackage p = new UnleashPackage(header_code, o);
        try {
            Log.v("SOCKC", "Client " + user.getNumber() + " Sending user info, header: " +
                    p.getHeader() + " with data " + p.getData());

            oos.writeObject(p);
            Log.v("SOCKC", "Client " + user.getNumber() + " UnleashPacket sent over the OOS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
