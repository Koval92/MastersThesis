package com.example.opencv;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = "OpenCV";
    public static final int MAX_THREADS = 1;
    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int threadsCount = 0;
    private Rect[] facesArray = new Rect[0];

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(null); // non-parametrized isn't working
            cascadeClassifier.load(mCascadeFile.getAbsolutePath()); // is not working, when path is in constructor
            if(cascadeClassifier.empty()) {
                throw new Exception("Cascade classifier is empty!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cascade", e);
        }

        openCvCameraView.enableView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set camera id and max resolution
        openCvCameraView = new JavaCameraView(this, 0);
        openCvCameraView.setMaxFrameSize(400, 300);

        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(final Mat aInputFrame) {
        // TODO race condition?
        // TODO measure time between threads finish

        if(threadsCount < MAX_THREADS) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    threadsCount++;

                    Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
                    MatOfRect faces = new MatOfRect();

                    long startTime = System.nanoTime();
                    cascadeClassifier.detectMultiScale(grayscaleImage, faces);
                    long endTime = System.nanoTime();
                    long duration = (endTime-startTime)/1000000;

                    facesArray = faces.toArray();
                    Log.i(TAG, "Detected " + facesArray.length + " faces in " + duration + " ms");

                    threadsCount--;
                }
            }).start();
        }

        for (Rect faceRect : facesArray) {
            Imgproc.rectangle(aInputFrame, faceRect.tl(), faceRect.br(), new Scalar(0, 255, 0, 255), 3);
        }

        return aInputFrame;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback)) {
            Log.e(TAG, "OpenCVLoader initAsync error!");
        }
    }
}
