package com.example.facedetector;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 4564;
    private ImageView imageView;
    private Button detectFacesButton;
    private Button makePhotoButton;
    private Button selectPhotoButton;
    private SeekBar scaleSeekBar;
    private TextView scaleTextView;
    private TextView pathTextView;
    private String imagePath;
    private Bitmap thumbnail;
    private int originalWidth;
    private int originalHeight;
    private int scaledWidth;
    private int scaledHeight;
    private Set<String> imagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeLayoutObjects();
        setOnClickListeners();
        imagePaths = Utils.loadImagePaths(getApplicationContext()) ;
        imagePaths.add(Utils.TEST_IMAGE_PATH);
        Utils.saveImagePaths(getApplicationContext(), imagePaths);
        replaceImage(Utils.TEST_IMAGE_PATH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals(getString(R.string.options))) {
            Intent intent = new Intent(getApplicationContext(), OptionsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void initializeLayoutObjects() {
        imageView = (ImageView) this.findViewById(R.id.previewImageView);
        detectFacesButton = (Button) this.findViewById(R.id.detectFacesButton);
        makePhotoButton = (Button) this.findViewById(R.id.makePhotoButton);
        selectPhotoButton = (Button) this.findViewById(R.id.selectPhotoButton);
        scaleSeekBar = (SeekBar) this.findViewById(R.id.scaleSeekBar);
        scaleTextView = (TextView) this.findViewById(R.id.sizeTextView);
        pathTextView = (TextView) this.findViewById(R.id.pathTextView);
    }

    private void setOnClickListeners() {
        makePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                intent.putExtra(getString(R.string.chosen_path_extra), imagePath);
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });

        detectFacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("detectFacesButton", "clicked");
                FaceDetectorTask task = new FaceDetectorTask(getApplicationContext(), imagePath, scaledWidth, scaledHeight) {
                    @Override
                    protected void onPostExecute(Bitmap image) {
                        super.onPostExecute(image);
                        onDetectionFinishes(image);
                    }
                };
                task.execute();
            }
        });

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scaledWidth = originalWidth * progress / 100;
                scaledHeight = originalHeight * progress / 100;
                scaleTextView.setText(getString(R.string.image_size, scaledWidth, scaledHeight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("scaleSeekBar", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("scaleSeekBar", "onStopTrackingTouch");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            String lastImagePath = getLastImagePath();
            imagePaths.add(lastImagePath);
            Utils.saveImagePaths(getApplicationContext(), imagePaths);
            replaceImage(lastImagePath);
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            String path = data.getStringExtra(getString(R.string.chosen_path_extra));
            replaceImage(path);
        } else {
            Toast.makeText(this, "Unknown activity result", Toast.LENGTH_SHORT).show();
        }
    }

    // TODO make it work on cyanogen?
    public String getLastImagePath() {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToLast()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
            cursor.close();
        } else {
            Log.e("lastImagePath", "null cursor or no files");
        }

        Log.i("Last Image Path", (path == null) ? "null" : path);
        return path;
    }

    private void replaceImage(String path) {
        imagePath = path;
        thumbnail = null;
        Bitmap image = BitmapFactory.decodeFile(imagePath);

        if (image == null) {
            Log.e("replaceImage", "image is null");
            Toast.makeText(this, "Couldn't load image", Toast.LENGTH_SHORT).show();
            Bitmap.Config config = Bitmap.Config.RGB_565;
            image = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_WIDTH, config);
        }

        originalWidth = image.getWidth();
        originalHeight = image.getHeight();

        int dstWidth = THUMBNAIL_WIDTH;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();

        thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
        imageView.setImageBitmap(thumbnail);
        scaleSeekBar.setProgress(100);
        pathTextView.setText(getString(R.string.path, path));

        Log.i("imagePath", (path == null) ? "null" : path);
        Log.i("image", originalWidth + " x " + originalHeight);
        Log.i("thumb", thumbnail.getWidth() + " x " + thumbnail.getHeight());
    }

    public void onDetectionFinishes(Bitmap image) {
        int dstWidth = THUMBNAIL_WIDTH;
        int dstHeight = image.getHeight() * dstWidth / image.getWidth();

        thumbnail = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
        imageView.setImageBitmap(thumbnail);
    }
}
