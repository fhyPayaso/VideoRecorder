package com.fhypayaso.video.business;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fhypayaso.video.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author fhyPayaso
 * @since 2018/8/2 on 下午1:54
 * fhyPayaso@qq.com
 */
public class ResultActivity extends AppCompatActivity {


    @BindView(R.id.surface_result)
    SurfaceView mSurfaceResult;

    private Display currentDisplay;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        try {
            playVideo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playVideo() throws IOException {

        String path = Environment.getExternalStorageDirectory().getPath() + "/record.mp4";
        mediaPlayer = new MediaPlayer();
        surfaceHolder = mSurfaceResult.getHolder();
        surfaceHolder.setFixedSize(100, 100);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(path);
        try {
            mediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setDisplay(surfaceHolder);
                mediaPlayer.start();
            }
        });


    }


}
