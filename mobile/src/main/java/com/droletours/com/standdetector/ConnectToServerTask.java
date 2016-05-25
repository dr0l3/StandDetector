package com.droletours.com.standdetector;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Rune on 27-04-2016.
 */
public class ConnectToServerTask extends AsyncTask<String,Void,DataOutputStream> {
    @Override
    protected DataOutputStream doInBackground(String... params) {
        String IP = params[0];
        int port = Integer.parseInt(params[1]);
        Socket socket = null;
        try {
            socket = new Socket(IP,port);
            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            return dos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
