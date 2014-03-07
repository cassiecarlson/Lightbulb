package com.kylealar.lightbulb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.hardware.Camera;
import android.content.pm.PackageManager;
//import android.view.WindowManager;
import android.hardware.Camera.Parameters;
import android.util.Log;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
import android.view.View;

//used for nexus phones and tablets
//cannot display flashlight without surface view of camera
//1x1 will work
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


public class MainActivity extends Activity implements Callback {

    public ImageButton powerButton;

    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Parameters params;
    //MediaPlayer mediaPlayer;
    public SurfaceHolder myHolder;
    public SurfaceView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //need to keep screen awake to keep flashlight on
        //can use this or set in xml for activity
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
          //      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //flashlight power button
        powerButton = (ImageButton) findViewById(R.id.powerButton);

        //check if hardware flash is supported
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        //no hardware flash
        //show error message for now, blank white screen with max brightness in the future

        //check if there is no hasFlash, avoid catching null pointer exception
        if (!hasFlash) {
            Log.d("NO FLASH PRESENT"," " + "DEVICE DOES NOT HAVE FLASH");
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            alert.setTitle("Error");
            alert.setMessage("Your device does not support hardware flashlights. " +
                    "A software solution is planned for future release. " +
                    "The app will now close");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //close the application for now
                    finish();
                }
            });
            alert.show();
            return;
        }

        //create surface view and holder to allow nexus to use flash
        try {
        preview = (SurfaceView) findViewById(R.id.PREVIEW);
        myHolder = preview.getHolder();
        myHolder.addCallback(this);
        camera = Camera.open();
        camera.setPreviewDisplay(myHolder);
        Log.d("SURFACE HOLDER"," " + "ADDING THE CAMERA PREVIEW TO THE SURFACE HOLDER");
        }
        catch (Exception e) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            alert.setTitle("Error");
            alert.setMessage("This app has some issues on Nexus devices. " +
                    "Wait for bug fixes.");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //close the application for now
                            finish();
                        }
                    });
            alert.show();
            return;
        }

        getCamera();
        toggleButtonImage();

        powerButton.setOnClickListener(new View.OnClickListener() {
            //turn flash on or off depending on current state when button clicked
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    flashOff();
                } else {
                    flashOn();
                }
                Log.d("CLICKING THE BUTTON!!! !:!:! !:! ", "" + v.getId());
            }
        });
    }

    //surface holder for nexus camera
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //surface holder for nexus camera
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("SURFACE CREATED"," " + "surfaceCreated");
        try {
        myHolder = holder;
        camera.setPreviewDisplay(myHolder);
        }
        catch (Exception e){
            finish();
        }
    }

    //surface holder for nexus camera
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("SURFACE DESTROYED"," " + "surfaceDestroyed");
        camera.stopPreview();
        myHolder = null;
    }

    //remove if statements from flashOn and flashOff
    //call the right one from the button click

    private void flashOn() {
        Log.d("FLASH ON"," " + "flashOn");
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
        //play sound when clicking button
        //powerButtonNoise();

        //get parameters of camera
        params = camera.getParameters();

        //set flash mode to torch for parameter
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);

        //set parameter of the camera to the parameter we just got
        camera.setParameters(params);

        //start camera to turn on flash
        camera.startPreview();
        isFlashOn = true;

        //change button image between on and off
        toggleButtonImage();
        }
    }

    private void flashOff() {
        Log.d("FLASH OFF"," " + "flashOff");
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
        //powerButtonNoise();

        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);

        camera.setParameters(params);
        camera.stopPreview();

        isFlashOn = false;

        toggleButtonImage();
        }
    }

    //change image resource for button based on current state at method call
    private void toggleButtonImage() {
        Log.d("TOGGLE IMAGE"," " + "toggleButtonImage");
        if(isFlashOn) {
            powerButton.setImageResource(R.drawable.flashlight_on);
        } else {
            powerButton.setImageResource(R.drawable.flashlight_off);
        }
    }

    //need to define a subtitle manager?
    //play a sound when the method is called
    /*
    private void powerButtonNoise() {
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.button_click);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //release the media player one the sound is played
                mp.release();
            }
        });
    }
    */

    //call this on start, get the camera parameters
    private void getCamera() throws NullPointerException {
        Log.d("CAMERA"," " + "getCamera");
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            }
            catch (Exception e) {
                Log.e("Camera failed to Open. Error: ", e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY","" + "onDestroy");
    }

    //turn off flashlight when app is paused
    @Override
    protected void onPause() {
        super.onPause();
        flashOff();
        Log.d("PAUSE"," " + "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("RESTART"," " + "onRestart");
    }

    //turn the flashlight back on if the app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RESUME"," " + "onResume");
        //make sure to check that the device has a flash in the first place!
        if(hasFlash) {
            flashOn();
            Log.d("RESUME: ", "Starting flashlight from resume " + "onResume");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("START"," " + "onStart");
        //get camera parameters
        getCamera();
    }

    //release the camera on stop
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("STOP"," " + "onStop");
        //if a camera is present, release and set camera to null
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }
}
