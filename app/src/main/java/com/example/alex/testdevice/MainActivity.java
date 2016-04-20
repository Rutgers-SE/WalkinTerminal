package com.example.alex.testdevice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://gg-is.herokuapp.com");
        } catch (URISyntaxException e) {}
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.connect(); // Something
        setContentView(R.layout.activity_main);
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


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
    }

}
