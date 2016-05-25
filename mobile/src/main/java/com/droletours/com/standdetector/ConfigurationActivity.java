package com.droletours.com.standdetector;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by Rune on 19-04-2016.
 */
public class ConfigurationActivity extends Activity {
    public static final String BIAS_CORRECTIONS = "BiasCorrectionCoefficients";
    private final int NUMBER_OF_MEASUREMENTS_REQUIRED = 100;
    private ImageView positionViewer;
    private TextView instructionTextView;
    private Queue<float[]> latestGravities;
    private Queue<float[]> latestAccelerations;
    private boolean gravityFulfillsRequirements;
    private PhoneDirection currentDirection;
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mAccelerationSensor;
    private double currentGravity;
    private int currentGravityUpdateCounter;
    private TextView gravityTextView;
    private double GRAVITY_LOWER_CAP = 9.75;
    private double GRAVITY_UPPER_CAP = 9.85;
    private ScheduledThreadPoolExecutor mScheduler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        positionViewer = (ImageView) findViewById(R.id.imageViewPosition);
        instructionTextView = (TextView) findViewById(R.id.InstructionTextView);
        gravityTextView = (TextView) findViewById(R.id.textViewShowGravity);

        mScheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        latestAccelerations = new LinkedBlockingQueue<>();
        latestGravities = new LinkedBlockingQueue<>();
        gravityFulfillsRequirements = false;
        currentGravity = 0;
        currentGravityUpdateCounter = 0;
        currentDirection = PhoneDirection.X_AXIS_DIRECTION_POSITIVE;
    }

    public void startConfiguration(View view) {
        mSensorManager.registerListener(accelerationListener, mAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(gravityListener, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        mScheduler.submit(new Runnable() {
            @Override
            public void run() {
                new UpdateUI().execute(PhoneDirection.X_AXIS_DIRECTION_POSITIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.X_AXIS_DIRECTION_POSITIVE);


                new UpdateUI().execute(PhoneDirection.Y_AXIS_DIRECTION_POSITIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.Y_AXIS_DIRECTION_POSITIVE);


                new UpdateUI().execute(PhoneDirection.Z_AXIS_DIRECTION_POSITIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.Z_AXIS_DIRECTION_POSITIVE);

                new UpdateUI().execute(PhoneDirection.X_AXIS_DIRECTION_NEGATIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.X_AXIS_DIRECTION_NEGATIVE);

                new UpdateUI().execute(PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE);


                new UpdateUI().execute(PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE);
                new WaitForCorrectGravity().execute(PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE);

                new CompleteConfiguration().execute();
            }
        });

    }

    public void updateGravityStatus(PhoneDirection direction){

        if(latestGravities.size() < NUMBER_OF_MEASUREMENTS_REQUIRED)
            return;

        if(direction == PhoneDirection.X_AXIS_DIRECTION_POSITIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[0] > GRAVITY_UPPER_CAP || measurements[0] < GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        if(direction == PhoneDirection.Y_AXIS_DIRECTION_POSITIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[1] > GRAVITY_UPPER_CAP || measurements[1] < GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        if(direction == PhoneDirection.Z_AXIS_DIRECTION_POSITIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[2] > GRAVITY_UPPER_CAP || measurements[2] < GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        if(direction == PhoneDirection.X_AXIS_DIRECTION_NEGATIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[0] < -GRAVITY_UPPER_CAP || measurements[0] > -GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        if(direction == PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[1] < -GRAVITY_UPPER_CAP || measurements[1] > -GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        if(direction == PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE){
            for (float[] measurements : latestGravities) {
                if (measurements[2] < -GRAVITY_UPPER_CAP || measurements[2] > -GRAVITY_LOWER_CAP) {
                    return;
                }
            }
        }

        gravityFulfillsRequirements = true;
    }

    private float calculateAverageForUI(PhoneDirection currentDirection, Queue<float[]> queue){
        float mod = (currentDirection.toString().contains("NEGATIVE"))? -1 : 1;
        float avg = calculateAverage(currentDirection,queue);
        return avg*mod;
    }

    private float calculateAverage(PhoneDirection currentDirection, Queue<float[]> queue) {
        float total_gravity = 0;
        for (float[] measurement : queue) {
            if(currentDirection == PhoneDirection.X_AXIS_DIRECTION_POSITIVE || currentDirection == PhoneDirection.X_AXIS_DIRECTION_NEGATIVE)
                total_gravity+= measurement[0];
            if(currentDirection == PhoneDirection.Y_AXIS_DIRECTION_POSITIVE || currentDirection == PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE)
                total_gravity+= measurement[1];
            if(currentDirection == PhoneDirection.Z_AXIS_DIRECTION_POSITIVE || currentDirection == PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE)
                total_gravity+= measurement[2];
        }

        return total_gravity / latestGravities.size();
    }

    private float calculateBiasFromReadings(Queue<float[]> gravities, Queue<float[]> acclerations, PhoneDirection direction) {
        float mean_acc = calculateAverage(direction, acclerations);
        float mean_gra = calculateAverage(direction, gravities);

        return -(1-(mean_acc/mean_gra));
    }


    public void updateSharedPreference(PhoneDirection direction){
        SharedPreferences.Editor editor = getSharedPreferences(BIAS_CORRECTIONS, 0).edit();
        //copy the data
        float bias = calculateBiasFromReadings(new LinkedList<>(latestGravities), new LinkedList<>(latestAccelerations), direction);
        gravityFulfillsRequirements = false;
        currentDirection = getNextDirection(direction);
        Log.d("stuff", "updating preferences");
        if(direction == PhoneDirection.X_AXIS_DIRECTION_POSITIVE){
            editor.putFloat("bias_x_pos", bias);
        }

        if(direction == PhoneDirection.Y_AXIS_DIRECTION_POSITIVE){
            editor.putFloat("bias_y_pos", bias);
        }

        if(direction == PhoneDirection.Z_AXIS_DIRECTION_POSITIVE){
            editor.putFloat("bias_z_pos", bias);
        }

        if(direction == PhoneDirection.X_AXIS_DIRECTION_NEGATIVE){
            editor.putFloat("bias_x_neg", bias);
        }

        if(direction == PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE){
            editor.putFloat("bias_y_neg", bias);
        }

        if(direction == PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE){
            editor.putFloat("bias_z_neg", bias);
        }
        editor.apply();

    }

    private PhoneDirection getNextDirection(PhoneDirection direction) {
        if(direction == PhoneDirection.X_AXIS_DIRECTION_POSITIVE)
            return PhoneDirection.Y_AXIS_DIRECTION_POSITIVE;
        if(direction == PhoneDirection.Y_AXIS_DIRECTION_POSITIVE)
            return PhoneDirection.Z_AXIS_DIRECTION_POSITIVE;
        if(direction == PhoneDirection.Z_AXIS_DIRECTION_POSITIVE)
            return PhoneDirection.X_AXIS_DIRECTION_NEGATIVE;
        if(direction == PhoneDirection.X_AXIS_DIRECTION_NEGATIVE)
            return PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE;
        if(direction == PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE)
            return PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE;
        return PhoneDirection.X_AXIS_DIRECTION_POSITIVE;
    }

    SensorEventListener accelerationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(latestAccelerations.size() < NUMBER_OF_MEASUREMENTS_REQUIRED)
                latestAccelerations.add(event.values.clone());
            else {
                latestAccelerations.poll();
                latestAccelerations.add(event.values.clone());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener gravityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(latestGravities.size() < NUMBER_OF_MEASUREMENTS_REQUIRED) {
                latestGravities.add(event.values.clone());
            }
            else {
                latestGravities.poll();
                latestGravities.add(event.values.clone());
            }
            updateGravityStatus(currentDirection);
            if(currentGravityUpdateCounter > 25){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("stuff", "updating view");
                        float mean_gravity = calculateAverageForUI(currentDirection, new LinkedList<>(latestGravities));
                        gravityTextView.setText(String.valueOf(mean_gravity));
                        gravityTextView.invalidate();
                    }
                });
                currentGravityUpdateCounter = 0;
            } else {
                currentGravityUpdateCounter++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    class WaitForCorrectGravity extends AsyncTask<PhoneDirection, Void, Void> {

        @Override
        protected Void doInBackground(PhoneDirection... params) {
            boolean status = getGravityStatus();
            while(!status){
                status = getGravityStatus();
                try {
                    Thread.sleep(200);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            updateSharedPreference(currentDirection);
            return null;
        }
    }

    private boolean getGravityStatus() {
        return gravityFulfillsRequirements;
    }

    class UpdateUI extends AsyncTask<PhoneDirection, Void, Void>{

        @Override
        protected Void doInBackground(final PhoneDirection... params) {
            runOnUiThread(new Runnable() {@Override public void run() {
                PhoneDirection direction = params[0];
                if(direction == PhoneDirection.X_AXIS_DIRECTION_POSITIVE)
                    positionViewer.setImageResource(R.drawable.x_axis_pos);
                if(direction == PhoneDirection.Y_AXIS_DIRECTION_POSITIVE)
                    positionViewer.setImageResource(R.drawable.y_axis_pos);
                if(direction == PhoneDirection.Z_AXIS_DIRECTION_POSITIVE)
                    positionViewer.setImageResource(R.drawable.z_axis_pos);
                if(direction == PhoneDirection.X_AXIS_DIRECTION_NEGATIVE)
                    positionViewer.setImageResource(R.drawable.x_axis_neg);
                if(direction == PhoneDirection.Y_AXIS_DIRECTION_NEGATIVE)
                    positionViewer.setImageResource(R.drawable.y_axis_neg);
                if(direction == PhoneDirection.Z_AXIS_DIRECTION_NEGATIVE)
                    positionViewer.setImageResource(R.drawable.z_axis_neg);
                positionViewer.invalidate();
                currentDirection = params[0];
            }});
            return null;
        }
    }

    class CompleteConfiguration extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            mSensorManager.unregisterListener(accelerationListener);
            mSensorManager.unregisterListener(gravityListener);
            finish();
            return null;
        }
    }
}