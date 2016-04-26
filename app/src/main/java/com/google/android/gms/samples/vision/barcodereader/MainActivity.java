
package com.google.android.gms.samples.vision.barcodereader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends Activity implements View.OnClickListener {

    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    private static final int CAPTURE_TIME_ACTIVITY_REQUEST_CODE = 100;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        deviceName = "entrance-terminal";
        deviceType = "terminal";
        mSocket.connect();

        try {
            JSONObject devSetupPayload = new JSONObject();
            devSetupPayload.put("name", deviceName)
                    .put("deviceType", deviceType);
            mSocket.emit("dev:setup", devSetupPayload);
            setContentView(R.layout.activity_main);
        } catch (JSONException e) {
            return;
        }

        findViewById(R.id.read_barcode).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    try{
                        JSONObject devSetupPayload = new JSONObject();
                        JSONObject devStatus = new JSONObject();

                        devStatus.put("qr-data", barcode.displayValue);
                        devStatus.put("action-type", "reservation" );

                        devSetupPayload.put("name", "entrance-terminal");
                        devSetupPayload.put("status", devStatus);

                        mSocket.emit("dev:trigger", devSetupPayload );
                    } catch (JSONException e){
                        return;
                    }
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mSocket.disconnect();
    }
}