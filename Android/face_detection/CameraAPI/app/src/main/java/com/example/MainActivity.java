package com.example;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "HelloOpenCV";
    private static final String FACE = "face";

    Camera mCamera;
    SurfaceView mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPreview = (SurfaceView)findViewById(R.id.surfaceView);
        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCamera = Camera.open(0); // 0 - back, 1 - front camera
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopFaceDetection();
        mCamera.stopPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        String allSizes = "";
        for(Camera.Size size : sizes) {
            allSizes = allSizes + size.width + "x" + size.height + " ";
        }
        Log.i(TAG, "Sizes: " + allSizes);

        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width,selected.height);
        mCamera.setParameters(params);

        mCamera.startPreview();
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            private long startTime = 0;
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                long currentTime = System.nanoTime();
                long duration = (currentTime - startTime) / 1000000;
                startTime = currentTime;
                Log.i(TAG, faces.length + " faces detected in " + duration + " ms");
//                for(Camera.Face face : faces) {
//                    Log.i(FACE, duration + " ms: " +face.rect.flattenToString());
//                }
            }
        });
        mCamera.startFaceDetection();
        Log.i(TAG, "face detection started");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG,"surfaceDestroyed");
    }
}
