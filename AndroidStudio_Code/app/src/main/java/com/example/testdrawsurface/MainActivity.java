package com.example.testdrawsurface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements UdpAsset {
    SeekBar redBar;
    SeekBar greenBar;
    SeekBar blueBar;

    CustomView customView;

    Button pipetButton;
    UDP_Client Client;
    //EditText ip_text;
    String esp_ip;
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Client = new UDP_Client();

        customView = findViewById(R.id.customView);
        customView.udpAsset = this;

        pipetButton = findViewById(R.id.pipet_button);
        customView.pipetButton = pipetButton;

        toggleButton = findViewById(R.id.toggleFill);
        toggleButton.setOnCheckedChangeListener(chipChangeListener);

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

        pipetButton.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue));

        //ip_text = findViewById(R.id.ip_address);
    }

    /*public void AutoGetIP(View v){
        RequestTask catTask = new RequestTask();
        catTask.execute();
    }

    class RequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpURLConnection connection = null;

            try {
                //Create connection
                URL url = new URL("http://test.e-school.in.ua/");
                connection = (HttpURLConnection) url.openConnection();

                //Get Response
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                ip_text.setText(line);
                rd.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        @Override
        protected void onPostExecute(String ip) {
            super.onPostExecute(ip);
            if(ip == null) return;
            char[] ipchar = ip.toCharArray();
            String text_ip = "";
            for(int i = 0; i < ipchar.length; i++) {
                if(ipchar[i] == '0' || ipchar[i] == '1' || ipchar[i] == '2' || ipchar[i] == '3' ||
                        ipchar[i] == '4' || ipchar[i] == '5' || ipchar[i] == '6' || ipchar[i] == '7' ||
                        ipchar[i] == '8' || ipchar[i] == '9' || ipchar[i] == '.') {
                    text_ip += ipchar[i];
                }
            }
            ip_text.setText(text_ip);
        }
    }*/
    private void ChangeColor(){
        int redValue, greenValue, blueValue;

        redValue = redBar.getProgress();
        greenValue = greenBar.getProgress();
        blueValue = blueBar.getProgress();
        pipetButton.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue));
        customView.draw_color.setColor(Color.rgb(redValue, greenValue, blueValue));
    }

    public void PipetButton(View view){
        customView.isPipet = true;
        pipetButton.setBackgroundColor(Color.BLACK);
    }

    public void ResetModeButton(View v){
        byte[] buttons_message = new byte[5];
        buttons_message[0] = 127;
        buttons_message[1] = 0;
        buttons_message[2] = 0;
        buttons_message[3] = 0;
        buttons_message[4] = 0;
        Client.SendMessage(ip_address, buttons_message);
    }

    public void ClearScreen(View view){
        customView.ClearButtons();
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ChangeColor();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private CompoundButton.OnCheckedChangeListener chipChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            customView.isDraw_fill = b;
        }
    };

    public void TetrisButton(View view){
        Intent intent = new Intent(this, TetrisActivity.class);
        intent.putExtra("ip_address", ip_address);
        startActivity(intent);
    }

    public void ToTextButton(View view){
        Intent intent = new Intent(this, TextActivity.class);
        intent.putExtra("ip_address", ip_address);
        startActivity(intent);
    }
}