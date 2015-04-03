package ca.drsystems.unleash;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.IOException;

//sound methods from PLAY removed to help with length
public class uSound {
    public Play playAct;
    public MediaPlayer mPlayer;
    public boolean sound_on;
    public SoundPool soundPool;
    protected int explosion_sound;
    public uSound(Play PlayAct,MediaPlayer MPlayer, SoundPool SPool){
        this.playAct = PlayAct;
        this.mPlayer = MPlayer;
        this.soundPool = SPool;
    }
    //start music playing if sound was toggled on
    public void startSound(){
        Intent intent = playAct.getIntent();
        sound_on = intent.getBooleanExtra("sound_on", true);
        if(sound_on){
            setSoundtrackLoop();
        }
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        explosion_sound = soundPool.load(playAct, R.raw.explosion, 1);
    }
    public void explosions()
    {
        soundPool.play(explosion_sound, 1.0f, 1.0f, 0, 0, 1.0f);
    }
    //set up soundtrack looping
    private void setSoundtrackLoop(){
        mPlayer = MediaPlayer.create(playAct, R.raw.unleashgameplay);
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
}
