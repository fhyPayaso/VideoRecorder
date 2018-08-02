package com.fhypayaso.video.business;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fhypayaso.video.R;
import com.fhypayaso.video.utils.FileUtil;
import com.fhypayaso.video.utils.ToastUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author fhyPayaso
 * @since 2018/8/2 on 上午11:49
 * fhyPayaso@qq.com
 */
public class VideoActivity extends AppCompatActivity {


    @BindView(R.id.surface_play)
    SurfaceView mSurfacePlay;
    @BindView(R.id.surface_record)
    SurfaceView mSurfaceRecord;


    // 播放
    private Display currentDisplay;
    private SurfaceHolder surfaceHolderPlayer;
    private MediaPlayer mMediaPlayer;


    // 录制
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder surfaceHolderRecorder;
    private Camera camera = null;
    private Camera.CameraInfo cameraInfo;
    private int cameraCount = 0;
    private String fileName = "/sdcard/record.mp4";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deletePlayer();
    }

    private void init() {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        // 申请相机和sd卡读写权限
        FileUtil.verifyStoragePermissions(this);
        initRecord();
    }


    private void playVideo() {
        mMediaPlayer = new MediaPlayer();
        surfaceHolderPlayer = mSurfacePlay.getHolder();
        surfaceHolderPlayer.setFixedSize(100, 100);
        surfaceHolderPlayer.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediaPlayer.reset();
        mMediaPlayer = MediaPlayer.create(VideoActivity.this, R.raw.m00349_0001);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setDisplay(surfaceHolderPlayer);
                mediaPlayer.start();
            }
        });
    }

    private void deletePlayer() {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }


    /**
     * 录制初始化
     */
    private void initRecord() {
        // 取得holder
        SurfaceHolder holder = mSurfaceRecord.getHolder();
        // holder加入回调接口
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
                surfaceHolderRecorder = surfaceHolder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                surfaceHolderRecorder = surfaceHolder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // surfaceDestroyed的时候同时对象设置为null
                mSurfaceRecord = null;
                surfaceHolderRecorder = surfaceHolder;
                mMediaRecorder = null;
            }
        });
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     * 开始录制
     */
    @OnClick(R.id.btn_start)
    public void onMBtnStartClicked() {
        initCamera();
        // 创建mediarecorder对象
        mMediaRecorder = new MediaRecorder();


        // 设置录制视频源为Camera(相机)
        mMediaRecorder.setCamera(camera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOrientationHint(270);


        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置录制的视频编码h263 h264
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        /* =================音频设置==================  */
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioEncodingBitRate(8000);
        mMediaRecorder.setAudioSamplingRate(8000);
        /* =================视频设置==================  */
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mMediaRecorder.setVideoSize(640, 480);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mMediaRecorder.setVideoFrameRate(20);
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mMediaRecorder.setPreviewDisplay(surfaceHolderRecorder.getSurface());
        // 设置视频文件输出的路径
        mMediaRecorder.setOutputFile(fileName);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playVideo();
    }

    @OnClick(R.id.btn_stop)
    public void onMBtnStopClicked() {

        if (mMediaRecorder != null) {
            // 停止录制
            mMediaRecorder.stop();
            // 释放资源
            mMediaRecorder.release();
            mMediaRecorder = null;
            ToastUtil.showToast("文件保存于 : " + fileName);
        }
    }


    /**
     * 初始化相机
     */
    private void initCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                    camera.setDisplayOrientation(90);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        camera.unlock();
    }

    @OnClick(R.id.btn_show)
    public void onViewClicked() {
        startActivity(new Intent(VideoActivity.this,ResultActivity.class));
    }
}
