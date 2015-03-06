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

            if(p != null){
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
                                Play.UserLocations.setUser(user);
                            }
                        });
                        break;
                    case START_CONDITIONS:
                        startCondition sc = (startCondition) p.getData();
                        Log.v("PORT","STCON" + sc.getReady());
                        PlayAct.startCount(sc.getReady());
                        break;
                    case USER_CLASS:
                        tmp_user = (User)p.getData();
                        Play.UserLocations.setUser(tmp_user);
                        Log.v("LOC", "Host Receiving user info: " + p.getData());
                        break;
                    case POWER_UP:
                        PowerUp powerIn = (PowerUp)p.getData();
                        Play.powerUpDecider.storedPowerUpList.add(powerIn);
                        long king = 999999999;
                        PowerUp winner =  powerIn;
                        boolean first = true;
                        for(PowerUp temp : Play.powerUpDecider.storedPowerUpList)
                        {
                            if (temp.getTime()<king) {
                                king = temp.getTime();
                                winner = temp;
                            }
                        }
                        PlayAct.hostService.sendToAll(POWER_UP,winner);
                        break;
                    case UNLEASH_D:
                        break;
                    case UNLEASH_C:
                        break;
                    default :
                        break;
                }
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
            Log.v("SOCKC", "ClientService.java: in SendThread");
            handler.post(new Runnable(){
                @Override
                public void run(){
                    Log.v("SOCKC", "ClientService.java: Getting UserLocations.returnList(), a list of all users");
                    userlist = Play.UserLocations.returnList();
                    Log.v("SOCKC", "ClientService.java: Userlist size: " + userlist.size());
                    Log.v("SOCKC", "User Info " + userlist);

                    for(User u : userlist.values()){
                        Log.v("SOCKC", "ClientService.java: Sending user in userlist: " + u.getNumber());
                        tmp_user = new User();
                        tmp_user.setLat(u.getLat());
                        tmp_user.setLon(u.getLon());
                        tmp_user.setName(u.getName());
                        tmp_user.setNumber(u.getNumber());
                        send(USER_CLASS, tmp_user);
                        Log.v("SOCKC", "ClientService.java: User " + u.getNumber() + "'s info sent");
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
                    Log.v("SOCKC", "Client " + user.getNumber() + " Started ReceiveThread");
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
                    Log.v("SOCKC", "Client " + user.getNumber() + " Started SendThread");
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
                Log.v("SOCKC", "Client " + user.getNumber() + " Received object" + p.getHeader());

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
        try {
            Log.v("SOCKC", "Client " + user.getNumber() +
                    " Create package, header should be INITIAL_PACKET_NUMBER the first time: " +
                    header_code);
            UnleashPackage p;

            Log.v("SOCKC", "Client " + user.getNumber() + " Sending user info, header: " + header_code);
            p = new UnleashPackage(header_code, o);

            oos.writeObject(p);
            Log.v("SOCKC", "Client " + user.getNumber() + " UnleashPacket sent over the OOS");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
