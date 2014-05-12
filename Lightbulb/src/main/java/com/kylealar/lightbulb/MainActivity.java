package com.kylealar.lightbulb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.hardware.Camera;
import android.content.pm.PackageManager;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.readystatesoftware.systembartint.SystemBarTintManager;


public class MainActivity extends Activity implements Callback {

    public ImageButton powerButton;

    public Camera camera;
    public boolean isFlashOn;
    public boolean hasFlash;
    Parameters params;
    public SurfaceHolder myHolder;
    public SurfaceView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("ON CREATE", "----------------------------" + "THIS IS THE FIRST LOG FOR APP START");

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(Color.parseColor("#dd5800"));

        powerButton = (ImageButton) findViewById(R.id.powerButton);

        //check if hardware flash is supported
        //show error message for now, blank white screen with max brightness in the future
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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
        } else {
            Log.d("HAS FLASH = TRUE", "NOTHING TO SEE HERE, NOT THE PROBLEM");
        }

        getCamera();
        toggleButtonImage();

        try {
            preview = (SurfaceView) findViewById(R.id.PREVIEW);
            myHolder = preview.getHolder();
            myHolder.addCallback(this);
            camera = Camera.open();
            camera.setPreviewDisplay(myHolder);
            Log.d("SURFACE HOLDER"," " + "ADDING THE CAMERA PREVIEW TO THE SURFACE HOLDER");
        }
        catch (Exception e) {
            Log.e("SURFACE HOLDER"," " + "I HAVE NO IDEA WHAT'S WRONG, BUT IT'S PROBABLY FINE");

        }

        flashOn();

        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    flashOff();
                    Log.d("CLICKING THE BUTTON", " " + "BUTTONS CALLS FLASH OFF");
                } else {
                    flashOn();
                    Log.d("CLICKING THE BUTTON", " " + "BUTTON CALLS FLASH ON");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                //aboutMe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void flashOn() {
        Log.d("FLASH ON"," " + "flashOn");
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();

        isFlashOn = true;

        toggleButtonImage();
        } else {
            Log.d("FLASH ON FAILED", " " + "flashOn else");
        }
    }

    private void flashOff() {
        Log.d("FLASH OFF"," " + "flashOff");
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();

        isFlashOn = false;

        toggleButtonImage();
        } else {
            Log.d("FLASH OFF FAILED", " " + "flashOff else");
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

    //call this on start, get the camera parameters
    private void getCamera() {
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

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("SURFACE CHANGED ", "SURFACE CHANGED " + "SURFACE CHANGED");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("SURFACE CREATED"," " + "surfaceCreated");
        try {
            myHolder = holder;
            camera.setPreviewDisplay(myHolder);
        }
        catch (Exception e){
            Log.e("Could not create surface holder. Error: ", e.getMessage());
            finish();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("SURFACE DESTROYED"," " + "surfaceDestroyed");
        camera.stopPreview();
        myHolder = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY","" + "onDestroy");
        if (camera!=null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
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

        /*
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(!hasFlash) {
            flashOn();
            Log.d("RESUME: ", "Starting flashlight from resume " + "onResume");
        }
        */

        //need to create surface view here to prevent crash
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
