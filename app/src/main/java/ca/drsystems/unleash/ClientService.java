package ca.drsystems.unleash;

/**
 * Created by SRoddick3160 on 2/16/2015.
 */

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

    public Socket client;
    public Handler handler;
    public Play PlayAct;
    public WifiP2pDevice device;
    private User user;
    boolean run;
    public InputStream is;
    public OutputStream os;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    private HashMap<Integer, User> userlist;
    private User tmp_user;
    private PowerUp tmp_power;
    private startCondition tmp_stc;
    private UnleashAttack tmp_unleash_c;
    final int INITIAL_PACKET_NUMBER = 255;
    final int START_CONDITIONS = 254;
    final int USER_CLASS = 253;
    final int POWER_UP = 252;
    final int UNLEASH_C = 251;
    final int UNLEASH_D = 250;



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

    private void sendUserInfoFirst(){
        Log.v("SOCKC", "Client " + user.getNumber() + " Sending initial packet");
        send(INITIAL_PACKET_NUMBER, user);
        run = true;
    }

    private void ReceiveThread(){
        while(true){
            Log.v("SOCKC", "Client " + user.getNumber() + " Calling receive()");
            UnleashPackage p = receive();

                int header = p.getHeader();
                switch (header)
                {
                    case INITIAL_PACKET_NUMBER:
                        Log.v("SOCKC", "ClientService.java: Received UnleashPackage with header = INITIAL_PACKET_NUMBER");
                        User u = (User)p.getData();

                        Log.v("SOCKC", "ClientService.java: Received UnleashPackage with user number = " + u.getNumber() + " and lat: " + u.getLat());

                        //this.user.setName(u.getName());
                        this.user.setLat(u.getLat());
                        this.user.setLon(u.getLon());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("SOCKC", "ClientService.java: Setting user: " + user.getNumber() + " into UserLocations with: " + user.getLat());
                                UserLocations.setUser(user);
                            }
                        });
                        break;
                    case START_CONDITIONS:
                        tmp_stc = (startCondition) p.getData();
                        Log.v("PORT","STRCON " + tmp_stc.getReady());
                        PlayAct.startCount(tmp_stc.getReady());
                        break;
                    case USER_CLASS:
                        /*tmp_user.setNumber(((User) p.getData()).getNumber());
                        tmp_user.setLat(((User) p.getData()).getLat());
                        tmp_user.setLon(((User) p.getData()).getLon());*/
                        tmp_user = (User)p.getData();
                        UserLocations.setUser(tmp_user);
                        Log.v("LOC", "Host Receiving user info: " + p.getData() + " holder " + tmp_user);
                        break;
                    case POWER_UP:
                        tmp_power = (PowerUp)p.getData();
                        PlayAct.hostService.decider(tmp_power);
                        break;
                    case UNLEASH_D:
                        break;
                    case UNLEASH_C:
                        tmp_unleash_c = (UnleashAttack)p.getData();
                        new AnimateUnleash(PlayAct,tmp_unleash_c.getLocation(), PlayAct,tmp_unleash_c.getPowerLvl(), true).execute();
                        PlayAct.hostService.sendToAll(UNLEASH_C,tmp_unleash_c);
                        break;
                    default :
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
            handler.post(new Runnable(){
                @Override
                public void run(){
                    userlist = UserLocations.returnList();
                    Log.v("SOCKC", "Userlist: " + userlist);

                    for(User u : userlist.values()){
                        tmp_user = new User();
                        tmp_user.setLat(u.getLat());
                        tmp_user.setLon(u.getLon());
                        tmp_user.setName(u.getName());
                        tmp_user.setNumber(u.getNumber());
                        send(USER_CLASS, tmp_user);
                        Log.v("SOCKC", "User " + u.getNumber() + "'s info sent with info: " + tmp_user.getLat());
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

    @Override
    protected String doInBackground(Void... arg0) {


        return null;
    }

    public UnleashPackage receive(){
        try {
            if(ois.available() == 0){
                Object o;
                o = ois.readObject();

                UnleashPackage p = (UnleashPackage)o;
                Log.v("SOCKC", "Client " + user.getNumber() + " Received object" + p.getHeader() + " with data " + p.getData());

                return p;
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OptionalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public void send(int header_code, Object o){
        UnleashPackage p = new UnleashPackage(header_code, o);
        try {
            Log.v("SOCKC", "Client " + user.getNumber() + " Sending user info, header: " +
                    p.getHeader() + " with data " + p.getData());

            oos.writeObject(p);
            Log.v("SOCKC", "Client " + user.getNumber() + " UnleashPacket sent over the OOS");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
