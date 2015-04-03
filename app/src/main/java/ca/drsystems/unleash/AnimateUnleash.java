package ca.drsystems.unleash;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class AnimateUnleash extends AsyncTask<Void, Void, Void> {
    LatLng location;
    Play playAct;
    CircleOptions circle;
    Circle rem_circle;
    Polyline rem_rect;
    PolylineOptions rect;
    Integer meters;
    Integer pic_num;
    boolean explosion_cir;
    //Constructor recieves call from ClientDeviceService or Play
    public AnimateUnleash(Play PlayAct, LatLng location, int power, boolean circle_expl) {
        Log.v("AU", "AnimateUnleash");
        this.playAct = PlayAct;
        this.location = location;
        playAct.circles = new ArrayList<Circle>();
        playAct.rects = new ArrayList<Polyline>();
        this.meters = power - 1;
        this.pic_num = 0;
        this.explosion_cir = circle_expl;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    //create a circle based on location of player at time of unleash call
    private void createCircle(int meters, int color){
        Log.v("CIRCLE", "createCircle()");
        circle = new CircleOptions();
        circle.center(new LatLng(location.latitude, location.longitude));
        circle.radius(meters);
        circle.fillColor(color);
        circle.strokeColor(0x00000000);
        //add circle to main thread
        playAct.handler.post(new Runnable() {
            @Override
            public void run() {
                playAct.addCircle(circle);
            }
        });
    }
    //create rectangle for directional unleash method that is not implemented in this build
    private void createRect(){
        double lat = location.latitude;
        double lon = location.longitude;

        // Instantiates a new Polyline object and adds points to define a rectangle
        rect = new PolylineOptions()
                .add(new LatLng(lat, lon))
                .add(new LatLng(lat + 0.00015, lon))  // North of the previous point, but at the same longitude
                .add(new LatLng(lat + 0.00015, lon + 0.00015))  // Same latitude, and 30km to the west
                .add(new LatLng(lat, lon + 0.00015))  // Same longitude, and 16km to the south
                .add(new LatLng(lat, lon)); // Closes the polyline.

        // Get back the mutable Polyline
        playAct.handler.post(new Runnable() {
            @Override
            public void run() {
                playAct.addRect(rect);
            }
        });
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.v("CIRCLE", "doInBackground");
        //check for type of unleash
        if(explosion_cir){
            try {
                createCircle(meters, Color.RED);
                //create multiple circles that expand their diameter while removing earlier circles to give explosion effect.
                Thread.sleep(500);
                int col = Color.argb(130, 255, 00, 00);
                createCircle(meters+2, col);

                Thread.sleep(500);
                col = Color.argb(50, 255, 00, 00);
                createCircle(meters+4, col);

                Thread.sleep(500);

                int length = playAct.circles.size();
                //removing circles
                for(int i = length - 1; i >= 0; i--){
                    rem_circle = playAct.circles.get(i);
                    //call to main thread
                    playAct.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            playAct.removeCircle(rem_circle);
                        }
                    });

                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else{
            createRect();
        }

        return null;
    }
}
