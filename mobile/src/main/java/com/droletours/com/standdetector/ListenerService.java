package com.droletours.com.standdetector;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by runed on 24-05-2016.
 */
public class ListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent event){
        Intent intent = new Intent("Message-event");
        intent.putExtra("message_path", event.getPath());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("debug", "onMessageReceived = " + event.getPath());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        Log.d("debug", "onDataChanged = " + dataEvents.toString());
    }
}
