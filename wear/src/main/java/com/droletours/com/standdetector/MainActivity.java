/*
 * Copyright 2015 Dejan Djurovski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droletours.com.standdetector;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity {
    private final String MESSAGE1_PATH = "/message1";
    private final String MESSAGE2_PATH = "/message2";

    private GoogleApiClient apiClient;
    private NodeApi.NodeListener nodeListener;
    private MessageApi.MessageListener messageListener;
    private String remoteNodeId;
    private Handler handler;

    private GestureDetector mGestureDetector;
    private DismissOverlayView mDismissOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlayView.setIntroText("Long press to exit");
        mDismissOverlayView.showIntroIfNecessary();

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            public void onLongPress(MotionEvent ev){
                mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
                mDismissOverlayView.show();
            }

            public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY){
                Log.d("debug", String.format("Scroll performed! (%1$f, %2$f) -> (%3$f, %4$f)", ev1.getRawX(), ev1.getRawY(), ev2.getRawX(), ev2.getRawY()));
                if(isCloseToEdge(ev2)){
                    SwipeDirection direction = getDirectionFromEvent(ev2);
                    handleCorrection(direction);
                    Log.d("debug", "Direction swiped = " + direction.toString());
                    return true;
                }
                return false;
            }
        });

        // Create NodeListener that enables buttons when a node is connected and disables buttons when a node is disconnected
        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Peer connected");
                startActivity(intent);
            }

            @Override
            public void onPeerDisconnected(Node node) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Peer disconnected");
                startActivity(intent);
            }
        };

        // Create GoogleApiClient
        apiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // Register Node and Message listeners
                Wearable.NodeApi.addListener(apiClient, nodeListener);
                //Wearable.MessageApi.addListener(apiClient, messageListener);
                // If there is a connected node, get it's id that is used when sending messages
                Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        Log.d("debug", "Getconnectednodesresult = " + getConnectedNodesResult.getStatus().getStatusMessage());
                        Log.d("debug", "Number of nodes = " +getConnectedNodesResult.getNodes().size());
                        if (getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                            remoteNodeId = getConnectedNodesResult.getNodes().get(0).getId();
                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        }).addApi(Wearable.API).build();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        return mGestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    private SwipeDirection getDirectionFromEvent(MotionEvent ev2) {
        double center = 160;
        double angle = Math.atan2(ev2.getRawX()-center, ev2.getRawY()-center)*180/ Math.PI;
        Log.d("debug", "X = " + ev2.getRawX() + " Y = " + ev2.getRawY() + " angle = " + angle);
        if(Math.abs(angle) > 135){
            return SwipeDirection.SWIPE_DIRECTION_UP;
        } else if(angle < -45){
            return SwipeDirection.SWIPE_DIRECTION_LEFT;
        } else if(angle < 45) {
            return SwipeDirection.SWIPE_DIRECTION_DOWN;
        } else {
            return SwipeDirection.SWIPE_DIRECTION_RIGHT;
        }
    }

    private void handleCorrection(SwipeDirection direction) {
        String message;
        switch (direction){
            case SWIPE_DIRECTION_DOWN:
                message = "/correction_sit";
                break;
            case SWIPE_DIRECTION_UP:
                message = "/correction_stand";
                break;
            case SWIPE_DIRECTION_LEFT:
                message = "/correction_null";
                break;
            case SWIPE_DIRECTION_RIGHT:
                message = "/correction_wrong";
                break;
            default:
                return;
        }
        Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, message, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
                if (sendMessageResult.getStatus().isSuccess()) {
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, sendMessageResult.getStatus().getStatusMessage());
                } else {
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, sendMessageResult.getStatus().getStatusMessage());
                }
                Log.d("debug","Correctionresult = " + sendMessageResult.getStatus().getStatusMessage());
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isCloseToEdge(MotionEvent ev) {
        float center = 160;
        double distance_to_center = getDistanceToCenter(ev,center);
        return distance_to_center>70;
    }

    private boolean isInMiddleRegion(MotionEvent ev) {
        float center = 160;
        double distance_to_center = getDistanceToCenter(ev, center);
        return distance_to_center<70;
    }

    private double getDistanceToCenter(MotionEvent ev, float center) {
        return Math.sqrt(Math.pow(Math.abs(ev.getRawX()-center),2) + Math.pow(Math.abs(ev.getRawY()-center),2));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check is Google Play Services available
        int connectionResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (connectionResult != ConnectionResult.SUCCESS) {
            // Google Play Services is NOT available. Show appropriate error dialog
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult, this, 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
        } else {
            apiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        // Unregister Node and Message listeners, disconnect GoogleApiClient and disable buttons
        Wearable.NodeApi.removeListener(apiClient, nodeListener);
        Wearable.MessageApi.removeListener(apiClient, messageListener);
        apiClient.disconnect();
        super.onPause();
    }
}
