package com.example.testdrawsurface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import kotlin.jvm.internal.ByteSpreadBuilder;

interface UdpAsset {
    UDP_Client Client = new UDP_Client();
    String ip_address = "192.168.4.1";
}
public class UDP_Client
{
    private AsyncTask<Void, Void, Void> async_cient;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    public void SendMessage(String ip_name, byte[] message)
    {
        async_cient = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                DatagramSocket ds = null;

                try
                {//192.168.137.173
                    //byte[] ipAddr = new byte[]{ (byte) 192, (byte) 168, (byte) 137, (byte) 173};
                    InetAddress addr = InetAddress.getByName(ip_name);
                    ds = new DatagramSocket(8888);
                    DatagramPacket dp;
                    dp = new DatagramPacket(message, message.length, addr, 8888);
                    ds.setBroadcast(true);
                    ds.send(dp);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (ds != null)
                    {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_cient.execute();
    }

    public void GetIPAddress() {
    }
}