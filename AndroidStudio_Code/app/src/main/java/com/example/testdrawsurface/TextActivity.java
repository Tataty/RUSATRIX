package com.example.testdrawsurface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class TextActivity extends AppCompatActivity implements UdpAsset {
    //EditText ip_text;
    String ip_address = "192.168.4.1";

    SeekBar redBar;
    SeekBar greenBar;
    SeekBar blueBar;
    CustomView customView;

    Button saveButton;
    UDP_Client Client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        //ip_text = findViewById(R.id.ip_address);
        //ip_text.setText(getIntent().getExtras().getString("ip_address"));

        customView = findViewById(R.id.customView);
        customView.udpAsset = this;
        customView.isAutoSend = false;

        saveButton = findViewById(R.id.pipet_button);

        redBar = (SeekBar) findViewById(R.id.redBar);
        greenBar = (SeekBar) findViewById(R.id.greenBar);
        blueBar = (SeekBar) findViewById(R.id.blueBar);

        redBar.setOnSeekBarChangeListener(seekBarChangeListener);
        greenBar.setOnSeekBarChangeListener(seekBarChangeListener);
        blueBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int redValue, greenValue, blueValue;
        redValue = redBar.getProgress();
        greenValue = greenBar.getProgress();
        blueValue = blueBar.getProgress();

        saveButton.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue));

        Client = new UDP_Client();

        //SendResetText(customView);
    }
    public void ClearButtons(View view){
        customView.ClearButtons();
    }

    public void SendResetText(View view){
        byte[] mode_message = new byte[5];
        mode_message[0] = 0;
        mode_message[1] = 0;
        mode_message[2] = 127;
        mode_message[3] = 0;
        mode_message[4] = 0;
        Client.SendMessage(ip_address, mode_message);
    }

    public void SendEndText(View view){
        byte[] mode_message = new byte[1];
        mode_message[0] = 92;
        Client.SendMessage(ip_address, mode_message);
    }

    public void SaveLetter(View view){ customView.PixelsSend(); }

    private void ChangeColor(){
        int redValue, greenValue, blueValue;
        redValue = redBar.getProgress();
        greenValue = greenBar.getProgress();
        blueValue = blueBar.getProgress();
        saveButton.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue));

        customView.draw_color.setColor(Color.rgb(redValue, greenValue, blueValue));
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ChangeColor();
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    public void BackButton(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}