package ca.drsystems.unleash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import java.io.IOException;


public class UnleashMain extends ActionBarActivity {
    ToggleButton sound;
    MediaPlayer mPlayer;
    boolean sound_activated;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unleash_main);
        sound_activated = false;

        sound = (ToggleButton)findViewById(R.id.soundToggle);
        sound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ToggleButton tb_sound = (ToggleButton)v;
                if(tb_sound.isChecked()){
                    if(sound_activated){
                        mPlayer.start();
                    } else{
                        setSoundtrackLoop();
                    }
                } else{
                    mPlayer.pause();
                }
            }
        });
    }
    private void setSoundtrackLoop(){
        sound_activated = true;
        mPlayer = MediaPlayer.create(this, R.raw.unleashsong);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mPlayer.setLooping(true);

        mPlayer.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unleash_main, menu);
        return true;
    }
    public void joinGame(View v)
    {
        Intent intent = new Intent(v.getContext(), Play.class);
        intent.putExtra("sound_on", sound.isChecked());
        startActivity(intent);
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
    @Override
    protected void onResume() {
        if(sound.isChecked()){
            mPlayer.start();
        }
        // TODO Auto-generated method stub
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean("firstLaunch", false);
        if(!previouslyStarted){

            launchTutorial();
        }
    }

    public void launchTutorial(){
        Intent intent = new Intent(this, Tutorial.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        if(sound.isChecked()){
            mPlayer.pause();
        }
        // TODO Auto-generated method stub
        super.onPause();
    }

    public void tutorialClick(View v){
        launchTutorial();
    }
}
