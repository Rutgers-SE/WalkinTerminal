package com.example.alex.testdevice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://gg-is.herokuapp.com");
        } catch (URISyntaxException e) {}
    }

    private String deviceName;
    private String deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        deviceName = "walkin-terminal";
        deviceType = "terminal";
        super.onCreate(savedInstanceState);
        mSocket.connect(); // Something



        try {
            JSONObject devSetupPayload = new JSONObject();


            devSetupPayload.put("name", deviceName)
                    .put("deviceType", deviceType);


            mSocket.emit("dev:setup", devSetupPayload);

            setContentView(R.layout.activity_main);
        } catch (JSONException e) {
            displayToast(e);
            return;
        }


    }

    private void displayToast(Exception e) {
        Log.d("TestDevice", e.toString());
        Context ctx = getApplicationContext();
        CharSequence failText = e.getMessage();
        int duration = Toast.LENGTH_LONG;
        Toast fail = Toast.makeText(ctx, failText, duration);
        fail.show();
    }

    private void toasty(String msg) {
        Context ctx = getApplicationContext();
        CharSequence failText = msg;
        int duration = Toast.LENGTH_LONG;
        Toast fail = Toast.makeText(ctx, failText, duration);
        fail.show();

    }

    public void sendQRData(View view) {
        try {
            TextView txtView = (TextView) findViewById(R.id.qrData);
            BarcodeDetector detector =
                    new BarcodeDetector.Builder(getApplicationContext())
                            .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                            .build();
            if(!detector.isOperational()){
                txtView.setText("Could not set up the detector!");
                return;
            }
        } catch (Exception e) {
            displayToast(e);
        }
    }

    public void attemptSend(View view) {
        EditText sendMessage = (EditText) findViewById(R.id.editText);
        String message = sendMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        sendMessage.setText("");
        mSocket.emit("test:external-device", message);
    }

    public void updateState(View view) {
        EditText devName = (EditText) findViewById(R.id.deviceName);
        EditText devType = (EditText) findViewById(R.id.deviceType);

        deviceName = devName.getText().toString();
        deviceType = devType.getText().toString();

        try {
            JSONObject devSetupPayload = new JSONObject();


            devSetupPayload.put("name", devName.getText().toString())
                    .put("deviceType", devType.getText().toString());


            mSocket.emit("dev:setup", devSetupPayload);
        } catch (JSONException e) {
            displayToast(e);
            return;
        }

    }

    public void loadQR(View view) {
        toasty("That good good");
        try {
            ImageView myImageView = (ImageView) findViewById(R.id.imgView);

            Bitmap myBitmap = BitmapFactory.decodeResource(
                    getApplicationContext().getResources(),
                    R.drawable.qrimg);

            myImageView.setImageBitmap(myBitmap);
        } catch (Exception e) {
            displayToast(e);
        }

    }

    public void qrOpen(View view) {

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            displayToast(e);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;



    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TestDevice");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("TestDevice", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
