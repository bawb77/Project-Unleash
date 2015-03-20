package ca.drsystems.unleash;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.IOException;

/**
 * Created by BBaxter3160 on 3/16/2015.
 */
public class uSound {
    Play playAct;
    MediaPlayer mPlayer;
    boolean sound_on;
    SoundPool soundPool;
    public uSound(Play PlayAct,MediaPlayer MPlayer, SoundPool SPool){
        this.playAct = PlayAct;
        this.mPlayer = MPlayer;
        this.soundPool = SPool;

    }
    public void startSound(){
        Intent intent = playAct.getIntent();
        sound_on = intent.getBooleanExtra("sound_on", true);
        if(sound_on){
            setSoundtrackLoop();
        }
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        playAct.explosion_sound = soundPool.load(playAct, R.raw.explosion, 1);
    }
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
