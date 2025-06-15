package com.example.testdrawsurface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TetrisActivity extends AppCompatActivity {

    UDP_Client Client;
    //EditText ip_text;
    String ip_address = "192.168.4.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        //ip_text = findViewById(R.id.ip_address);
        //ip_text.setText(getIntent().getExtras().getString("ip_address"));

        Client = new UDP_Client();
        //ResetModeButton(ip_text);
    }

    public void SnakeModeButton(View v){
        byte[] buttons_message = new byte[5];
        buttons_message[0] = 0;
        buttons_message[1] = 127;
        buttons_message[2] = 0;
        buttons_message[3] = 0;
        buttons_message[4] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }
    public void TetrisModeButton(View v){
        byte[] buttons_message = new byte[5];
        buttons_message[0] = 0;
        buttons_message[1] = 0;
        buttons_message[2] = 0;
        buttons_message[3] = 127;
        buttons_message[4] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void UpButton(View v){
        byte[] buttons_message = new byte[4];
        buttons_message[0] = 92;
        buttons_message[1] = 0;
        buttons_message[2] = 0;
        buttons_message[3] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void DownButton(View v){
        byte[] buttons_message = new byte[4];
        buttons_message[0] = 0;
        buttons_message[1] = 0;
        buttons_message[2] = 92;
        buttons_message[3] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void RightButton(View v) {
        byte[] buttons_message = new byte[4];
        buttons_message[0] = 0;
        buttons_message[1] = 92;
        buttons_message[2] = 0;
        buttons_message[3] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void LeftButton(View v){
        byte[] buttons_message = new byte[4];
        buttons_message[0] = 0;
        buttons_message[1] = 0;
        buttons_message[2] = 0;
        buttons_message[3] = 92;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void BackButton(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}