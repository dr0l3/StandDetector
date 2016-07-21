package com.droletours.com.standdetector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.droletours.com.standdetector.UtilClasses.CalculationUtil;
import com.droletours.com.standdetector.UtilClasses.ParseUtil;
import com.droletours.com.standdetector.UtilClasses.WekaUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.apache.commons.math3.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Core.BiasConfiguration;
import Core.ClassificationEventRecord;
import Core.ClassifierType;
import Core.SensorEventRecord;
import Core.Window;
import weka.classifiers.Classifier;
import weka.core.Instances;

@SuppressWarnings({"FieldCanBeLocal", "Convert2Lambda", "Anonymous2MethodRef", "WhileLoopReplaceableByForEach"})
public class MyActivity extends Activity implements UsernameDialogFragment.UserGroupNameListener {

    /** Constants */
    public String PARSE_SERVER_URI = "http://46.101.208.96:5050/parse/";
    public String PARSE_SERVER_APP_ID = "thesis-app-id";
    public static final String BIAS_CORRECTIONS = "BiasCorrectionCoefficients";
    public static final String CLASSIFIER_PARSE_OBJECT_ID = "ClassifierParseObjectId";
    public static final String USER_GROUP_NAME = "UserGroupName";
    private static final String CLASSIFICATIONS_FILE_NAME = "userclassificationstwentyfourhours";
    private static final String USER_SOUND_TOGGLE_STATUS = "soundtogglestatus";
    private String CLASSIFIER_OBJECT_ID;
    private double WINDOW_SIZE_SECONDS = 3;
    private double OVERLAP = (1.0/2.0);
    private double START_DELAY_SECONDS = 5;
    private Integer SIZE_OF_RECENT_WINDOWS = 10;
    private long MIN_TIME_BETWEEN_TAPS_IN_MILLIS = 8000;
    private long SHORT_BREAK = 250;
    private long SHORT_VIBRATE = 250;
    private long VERY_LONG_VIBRATE = 4000;
    private long START_IMMEDEATELY = 0;
    private long[] SIT_PATTERN = {START_IMMEDEATELY,
            SHORT_VIBRATE, SHORT_BREAK, SHORT_VIBRATE, SHORT_BREAK,
            SHORT_VIBRATE, SHORT_BREAK, SHORT_VIBRATE, SHORT_BREAK,
            SHORT_VIBRATE, SHORT_BREAK, SHORT_VIBRATE, SHORT_BREAK,
            SHORT_VIBRATE, SHORT_BREAK, SHORT_VIBRATE, SHORT_BREAK};
    private long[] STAND_PATTERN = {0,VERY_LONG_VIBRATE};

    /** Android resources */
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mAccelerometer;
    private Sensor mProximitySensor;
    private ScheduledThreadPoolExecutor mScheduler;
    private ScheduledFuture<?> classificationFuture;
    private PowerManager.WakeLock wakeLock;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private static SoundPool soundPool;
    private GoogleApiClient mGoogleApiClient;


    /** Weka related */
    private Classifier eventClassifier;
    private Classifier standsitClassifier;
    private String STARTING_EVENT_CLASSIFIER = "1464260566112_filteredclassifierevent_sniffer.model";
    private String STARTING_SITSTAND_CLASSIFIER = "1464260179839_filteredclassifiersit_stand_classifier.model";

    /** Application data */
    private List<SensorEventRecord> mSensorEventList;
    private float[] latestGravityEvent;
    private float[] latestProximityEvent;
    private Queue<Pair<Window, Classification>> recentWindows;
    private Queue<Classification> latest_events;
    private List<Classification> chart_events;
    private String username;
    private String groupname;
    private BiasConfiguration biasConfiguration;
    private static HashMap<Integer, Integer> soundPoolMap;

    /** UI / Misc data */
    private DecimalFormat decimalFormat;

    /** Widgets etc */
    private Switch networkSwitch;
    private Switch classificationSwitch;
    private Switch bluetoothSwitch;
    private EditText mIP;
    private EditText mPort;
    private BarChart mBarChart;

    private TextView mTextViewAccX;
    private TextView mTextViewAccY;
    private TextView mTextViewAccZ;
    private TextView mTextViewGraX;
    private TextView mTextViewGraY;
    private TextView mTextViewGraZ;
    private TextView mTextViewAccUp;
    private TextView mTextViewAccRest;
    private TextView mTextViewClassification;

    /** Control state */
    private boolean stream_to_server;
    private static boolean isParseInitialized = false;
    private int currentGlobalSoundSetting;
    private boolean toggleSoundStatus;
    private long time_of_last_tap;
    private BroadcastReceiver mRingermodeReceiver;
    private IntentFilter mRingermodeIntentFilter;
    private boolean event_in_last_cycle;

    /** Rest */
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<SensorEventRecord> sensorEventRecordJsonAdapter = moshi.adapter(SensorEventRecord.class);
    private JsonAdapter<ClassificationEventRecord> classificationEventRecordJsonAdapter = moshi.adapter(ClassificationEventRecord.class);
    private DataOutputStream mDos;

    /** Sound URI constants */
    private static final int TAP_DETECTED_SOUND = R.raw.tapdetected;
    private static final int STAND_UP_DETECTED_SOUND = R.raw.standupdetected;
    private static final int SIT_DOWN_DETECTED_SOUND = R.raw.sitdowndetected;

    /** Log codes */
    private static final String CLASSIFICATION_DEBUG = "Classification";
    private static final String SOUND_DEBUG = "Sound";
    private static final String TAG = "debug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mScheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        mScheduler.setRemoveOnCancelPolicy(true);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pwl");

        if(!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this).applicationId(PARSE_SERVER_APP_ID).server(PARSE_SERVER_URI).build());
            ParseInstallation.getCurrentInstallation().saveInBackground();
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            isParseInitialized = true;
        }

        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(Wearable.API)
                        .build();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("Message-event"));

        createSoundPool();
        soundPoolMap = new HashMap<>();
        int s1 = soundPool.load(getApplicationContext(), R.raw.tapdetected, 1);
        int s2 = soundPool.load(getApplicationContext(), R.raw.sitdowndetected, 2);
        int s3 = soundPool.load(getApplicationContext(), R.raw.standupdetected, 3);

        soundPoolMap.put(TAP_DETECTED_SOUND, s1 );
        soundPoolMap.put(SIT_DOWN_DETECTED_SOUND,s2 );
        soundPoolMap.put(STAND_UP_DETECTED_SOUND, s3);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        time_of_last_tap = 0;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRingermodeReceiver =new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                currentGlobalSoundSetting = mAudioManager.getRingerMode();
            }
        };
        mRingermodeIntentFilter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);

        latest_events = new LinkedBlockingQueue<>();
        chart_events = new ArrayList<>();

        mTextViewAccX = (TextView) findViewById(R.id.textViewAccX);
        mTextViewAccY= (TextView) findViewById(R.id.textViewAccY);
        mTextViewAccZ= (TextView) findViewById(R.id.textViewAccZ);
        mTextViewGraX= (TextView) findViewById(R.id.textViewGraX);
        mTextViewGraY= (TextView) findViewById(R.id.textViewGraY);
        mTextViewGraZ= (TextView) findViewById(R.id.textViewGraZ);
        mTextViewAccUp= (TextView) findViewById(R.id.textViewAccUp);
        mTextViewAccRest= (TextView) findViewById(R.id.textViewAccRest);
        mTextViewClassification = (TextView) findViewById(R.id.textViewClassification);

        mSensorEventList = new ArrayList<>();
        decimalFormat = new DecimalFormat("00.0000");

        mIP = (EditText) findViewById(R.id.editTextIP);
        mPort = (EditText) findViewById(R.id.editTextPort);
        networkSwitch = (Switch) findViewById(R.id.switch1);
        networkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stream_to_server = isChecked;
                if(isChecked){
                    String ip = String.valueOf(mIP.getText());
                    String port = String.valueOf(mPort.getText());
                    try {
                        mDos = new ConnectToServerTask().execute(ip, port).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        stream_to_server = networkSwitch.isChecked();
        recentWindows = new LinkedBlockingQueue<>();
        event_in_last_cycle = false;
        classificationSwitch = (Switch) findViewById(R.id.classificationSwitch);
        classificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    startClassification();
                } else {

                    stopClassification();
                }
            }
        });

        SharedPreferences usergroupSharedPreferences = getSharedPreferences(USER_GROUP_NAME, 0);
        username = usergroupSharedPreferences.getString("User_name", "dummy_user");
        groupname = usergroupSharedPreferences.getString("Group_name", "dummy_group");

        SharedPreferences usersoundtoggglestatus = getSharedPreferences(USER_SOUND_TOGGLE_STATUS, 0);
        toggleSoundStatus = usersoundtoggglestatus.getBoolean("soundtogglestatus", false);

        mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateClassifiersEventually();
            }
        }, 1,1,TimeUnit.HOURS);

        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                FileInputStream fis;
                try {
                    fis = openFileInput(CLASSIFICATIONS_FILE_NAME);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    List<Classification> fromFile = (List<Classification>) ois.readObject();
                    chart_events.addAll(fromFile);
                    Iterator<Classification> iter = chart_events.iterator();
                    while(iter.hasNext()){
                        Classification event = iter.next();
                        if( isOlderThan24Hours(event)){
                            chart_events.remove(event);
                        } else {
                            break;
                        }
                    }
                    updateBarChart();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("debug", "onReceive = " + intent.toString());
            String message_path = intent.getStringExtra("message_path");
            switch (message_path){
                case Correction.CORRECTION_SIT:
                    correctSit(null);
                    break;
                case Correction.CORRECTION_STAND:
                    correctStand(null);
                    break;
                case Correction.CORRECTION_NULL:
                    correctNull(null);
                    break;
                case Correction.CORRECTION_WRONG:
                    correctWrong(null);
                    break;
                default:
                    Log.d(TAG, "mMessageReceiver got unexpected path: " + message_path );
            }
        }
    };

    public void createSoundPool(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }


    @SuppressWarnings("deprecation")
    private void createOldSoundPool() {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();

        soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRingermodeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume(){
        registerReceiver(mRingermodeReceiver,mRingermodeIntentFilter);
        SharedPreferences biasConfigs = getSharedPreferences(BIAS_CORRECTIONS, 0);
        float bias_x_pos = biasConfigs.getFloat("bias_x_pos", 0);
        float bias_y_pos = biasConfigs.getFloat("bias_y_pos", 0);
        float bias_z_pos = biasConfigs.getFloat("bias_z_pos", 0);
        float bias_x_neg = biasConfigs.getFloat("bias_x_neg", 0);
        float bias_y_neg = biasConfigs.getFloat("bias_y_neg", 0);
        float bias_z_neg = biasConfigs.getFloat("bias_z_neg", 0);
        biasConfiguration = new BiasConfiguration(
                bias_x_pos,
                bias_y_pos,
                bias_z_pos,
                bias_x_neg,
                bias_y_neg,
                bias_z_neg);
        mTextViewClassification.setText(biasConfiguration.toString());

        //Handle ParseObject Wrapping of classifiers
        //if Object ID has already been set. Do nothing
        if(CLASSIFIER_OBJECT_ID == null){
            //if Object ID is not set, look in shared preferences
            SharedPreferences parseConfigs = getSharedPreferences(CLASSIFIER_PARSE_OBJECT_ID, 0);
            CLASSIFIER_OBJECT_ID = parseConfigs.getString("Object_ID", null);
            if(CLASSIFIER_OBJECT_ID == null){
                //if not in shared preferences then create a new object
                try
                {
                    InputStream isEventClassifier = getAssets().open(STARTING_EVENT_CLASSIFIER);
                    eventClassifier = (Classifier) weka.core.SerializationHelper.read(isEventClassifier);

                    InputStream isStandSitClassifier = getAssets().open(STARTING_SITSTAND_CLASSIFIER);
                    standsitClassifier = (Classifier) weka.core.SerializationHelper.read(isStandSitClassifier);

                } catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    ByteArrayOutputStream bos_evnt = new ByteArrayOutputStream();
                    ObjectOutputStream oos_evnt = new ObjectOutputStream(bos_evnt);

                    ByteArrayOutputStream bos_stst = new ByteArrayOutputStream();
                    ObjectOutputStream oos_stst = new ObjectOutputStream(bos_stst);

                    oos_evnt.writeObject(eventClassifier);
                    byte[] eventClassifierBytes = bos_evnt.toByteArray();
                    oos_evnt.flush();

                    ParseFile eventClassifierFile = new ParseFile("EventClassifier",eventClassifierBytes);
                    eventClassifierFile.saveInBackground();

                    oos_stst.writeObject(standsitClassifier);
                    byte[] sitstandClassifierBytes = bos_stst.toByteArray();
                    oos_stst.flush();

                    ParseFile sitstandClassifierFile = new ParseFile("SitStandClassifier", sitstandClassifierBytes);
                    sitstandClassifierFile.saveInBackground();

                    final ParseObject classifierObject = new ParseObject("ClassifierConfiguration");
                    classifierObject.put("Group", groupname);
                    classifierObject.put("Username", username);
                    classifierObject.put("EventClassifier", eventClassifierFile);
                    classifierObject.put("SitStandClassifier", sitstandClassifierFile);

                    classifierObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            CLASSIFIER_OBJECT_ID = classifierObject.getObjectId();

                            SharedPreferences.Editor editor = getSharedPreferences(CLASSIFIER_PARSE_OBJECT_ID, 0).edit();
                            editor.putString("Object_ID", CLASSIFIER_OBJECT_ID);
                            editor.apply();
                            //Log.d("stuff", "ClassifierObjectID Set = " + CLASSIFIER_OBJECT_ID);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                updateClassifiersEventually();
            }
        } else {
            updateClassifiersEventually();
        }

        if (CLASSIFIER_OBJECT_ID == null){
            //open the fragment
            openUsernameFragment();
        }

        if(eventClassifier == null) {
            //Log.d(TAG, "Classifier == null");
        } else {
            //Log.d(TAG, eventClassifier.toString());
        }

        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                //Create the x-vals
                updateBarChart();
            }
        });

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
        } else if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }

        super.onResume();
    }

    private void updateBarChart() {
        ArrayList<String> last_24_hours = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(getResources().getConfiguration().locale);
        Date now = calendar.getTime();
        int current_hour = (calendar.get(Calendar.HOUR_OF_DAY));
        current_hour++;
        int starting_hour = current_hour;
        //lav data således at der står 24 gange HOUR:XX
        for (int i = 0; i < 24; i++) {
            last_24_hours.add(String.valueOf(current_hour%24)+":XX");
            current_hour++;
        }

        //Create the y-vals
        ArrayList<Integer> stands = new ArrayList<>(Collections.nCopies(24, 0));
        for (Classification event : chart_events) {
            Date event_ts = event.getTimestamp();
            Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
            cal.setTime(event_ts);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int index = ((((hour-starting_hour)) % 24) + 24) % 24;
            stands.set(index, stands.get(index)+1);
        }

        ArrayList<BarEntry> bardata = new ArrayList<>();
        for (int i = 0; i < stands.size(); i++) {
            bardata.add(new BarEntry(stands.get(i), i));
        }

        BarDataSet barDataSet = new BarDataSet(bardata,"Stands");
        barDataSet.setDrawValues(false);
        barDataSet.setLabel("Standups detected");

        BarData data = new BarData(last_24_hours, barDataSet);

        int max = 0;
        for (Integer stand : stands) {
            if ( stand > max)
                max = stand;
        }

        mBarChart = (BarChart) findViewById(R.id.barchart);
        mBarChart.getAxisLeft().setAxisMinValue(0);
        mBarChart.getAxisRight().setAxisMinValue(0);
        mBarChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        mBarChart.setDescription("");
        mBarChart.setData(data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBarChart.invalidate();
            }
        });

    }

    private boolean isOlderThan24Hours(Classification event) {
        Calendar now = Calendar.getInstance(getResources().getConfiguration().locale);
        Calendar event_time = Calendar.getInstance(getResources().getConfiguration().locale);
        event_time.setTime(event.getTimestamp());
        return (now.getTime().getTime() - event_time.getTime().getTime()) > (24 * 60 * 60 * 1000);
    }

    @Override
    public void onPause(){
        super.onPause();
        FileOutputStream fos;
        try {
            fos = openFileOutput(CLASSIFICATIONS_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(chart_events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configBias(View view) {
        stopClassification();
        unregisterReceiver(mRingermodeReceiver);
        Intent configuration_intent = new Intent(this, ConfigurationActivity.class);
        startActivity(configuration_intent);
    }

    public void resetBias(View view) {
        SharedPreferences configs = getSharedPreferences(BIAS_CORRECTIONS, 0);
        float bias_x_pos = 0;
        float bias_y_pos = 0;
        float bias_z_pos = 0;
        float bias_x_neg = 0;
        float bias_y_neg = 0;
        float bias_z_neg = 0;
        biasConfiguration = new BiasConfiguration(
                bias_x_pos,
                bias_y_pos,
                bias_z_pos,
                bias_x_neg,
                bias_y_neg,
                bias_z_neg);
        SharedPreferences.Editor editor = getSharedPreferences(BIAS_CORRECTIONS, 0).edit();
        editor.putFloat("bias_x_pos", bias_x_pos);
        editor.putFloat("bias_y_pos", bias_y_pos);
        editor.putFloat("bias_z_pos", bias_z_pos);
        editor.putFloat("bias_x_neg", bias_x_neg);
        editor.putFloat("bias_y_neg", bias_y_neg);
        editor.putFloat("bias_z_neg", bias_z_neg);
        editor.apply();
        mTextViewClassification.setText(biasConfiguration.toString());
    }

    public void updateClassifierButtonPressed(View view) {
        Log.d("stuff", "UpdateClassifiersBUttonPressed");
        updateClassifiers();
    }

    public void updateClassifiersEventually(){
        if(!(CLASSIFIER_OBJECT_ID == null)){
            ParseQuery<ParseObject> query = new ParseQuery<>("ClassifierConfiguration");
            query.getInBackground(CLASSIFIER_OBJECT_ID, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e != null) {
                        Log.d("stuff", "Mistakes were made: " + e.getMessage());
                    } else {
                        try {
                            eventClassifier = (Classifier) weka.core.SerializationHelper.read(new ByteArrayInputStream(parseObject.getParseFile("EventClassifier").getData()));
                            standsitClassifier = (Classifier) weka.core.SerializationHelper.read(new ByteArrayInputStream(parseObject.getParseFile("SitStandClassifier").getData()));
                            Log.d("stuff", "classifiers updated");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void updateClassifiers(){
        Log.d("stuff", "Trying to update classifiers");
        if(!(CLASSIFIER_OBJECT_ID == null)){
            ParseQuery<ParseObject> query = new ParseQuery<>("ClassifierConfiguration");
            try {
                ParseObject parseObject = query.get(CLASSIFIER_OBJECT_ID);
                eventClassifier = (Classifier) weka.core.SerializationHelper.read(new ByteArrayInputStream(parseObject.getParseFile("EventClassifier").getData()));
                standsitClassifier = (Classifier) weka.core.SerializationHelper.read(new ByteArrayInputStream(parseObject.getParseFile("SitStandClassifier").getData()));
                Log.d("stuff", "Classifiers updated");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void classifiersToStringButtonPressed(View view) {
        Log.d("stuff", "eventClassifer: " + eventClassifier.toString());
        Log.d("stuff", "sitstandClassifer: " +standsitClassifier.toString());
    }

    public void correctSit(View view) {
        double max_probability = 0;
        Window max_probability_window = null;
        for (Pair<Window, Classification> pair : recentWindows) {
            //get probability of event
            double probability = pair.getValue().getEvent_distribution()[0];
            //filter windows that was classified as event
            if(probability < 0.5 && probability > max_probability){
                max_probability = probability;
                max_probability_window = pair.getKey();
            }
        }
        Correction correction = new Correction(Classification.NULL_CLASSIFICATION, "",Classification.EVENT_CLASSIFICATION, Classification.SIT_CLASSIFICATION);
        ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(max_probability_window, correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
    }

    public void correctStand(View view) {
        double max_probability = 0;
        Window max_probability_window = null;
        for (Pair<Window, Classification> pair : recentWindows) {
            //get probability of event
            double probability = pair.getValue().getEvent_distribution()[0];
            //filter windows that was classified as event
            if(probability < 0.5 && probability > max_probability){
                max_probability = probability;
                max_probability_window = pair.getKey();
            }
        }
        Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
        Date now = cal.getTime();
        chart_events.add(new Classification(Classification.EVENT_CLASSIFICATION, Classification.STAND_CLASSIFICATION, new double[]{1, 0}, new double[]{1,0}, now));
        updateBarChart();
        Correction correction = new Correction(Classification.NULL_CLASSIFICATION, "", Classification.EVENT_CLASSIFICATION, Classification.STAND_CLASSIFICATION);
        ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(max_probability_window, correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
    }

    public void correctWrong(View view) {
        int timestamp = 0;
        Window max_probability_window = null;
        String correctEventString = null;
        for (Pair<Window, Classification> pair : recentWindows) {
            Classification classification = pair.getValue();
            //find all the event windows
            if(classification.getEvent_classification().equals(Classification.EVENT_CLASSIFICATION)){
                Window window = pair.getKey();
                int ts = window.getListOfFeatureLines().get(0).getTimestamp();
                if(ts> timestamp){
                    timestamp = ts;
                    max_probability_window = window;
                    //sit eventString to the opposite of what the event was classified as
                    correctEventString = (classification.getSitstand_classification().contains(Classification.SIT_CLASSIFICATION))? Classification.STAND_CLASSIFICATION : Classification.SIT_CLASSIFICATION;
                }
            }
        }

        if (max_probability_window != null) {
            if(max_probability_window.getLabel().equals(Classification.STAND_CLASSIFICATION)){
                chart_events.remove(chart_events.size()-1);
                Log.d("chart", "removed stand");
                updateBarChart();
            }
            Correction correction = new Correction(Classification.EVENT_CLASSIFICATION, "", Classification.EVENT_CLASSIFICATION, correctEventString);
            ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(max_probability_window, correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
        }
    }

    public void correctNull(View view) {
        int timestamp = 0;
        Window max_probability_window = null;
        for (Pair<Window, Classification> pair : recentWindows) {
            Classification classification = pair.getValue();
            //find all the event windows
            if(classification.getEvent_classification().equals(Classification.EVENT_CLASSIFICATION)){
                Window window = pair.getKey();
                int ts = window.getListOfFeatureLines().get(0).getTimestamp();
                if(ts> timestamp){
                    timestamp = ts;
                    max_probability_window = window;
                }
            }
        }
        Log.d("chart", "removed stand");
        chart_events.remove(chart_events.size()-1);
        updateBarChart();
        Correction correction = new Correction(Classification.EVENT_CLASSIFICATION, "", Classification.NULL_CLASSIFICATION, "");
        ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(max_probability_window, correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
    }

    public void stopClassification(View view) {
        stopClassification();
    }

    private void stopClassification(){
        if(wakeLock.isHeld())
            wakeLock.release();
        mSensorManager.unregisterListener(gravityListener);
        mSensorManager.unregisterListener(accelerationListener);
        mSensorManager.unregisterListener(proximityListener);

        //stop classification
        if(classificationFuture != null)
            classificationFuture.cancel(false);
    }

    public void startClassification(View view) {
        startClassification();
    }

    private void startClassification(){
        //register listeners
        if(!wakeLock.isHeld())
            wakeLock.acquire();
        mSensorManager.registerListener(gravityListener,mGravitySensor,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accelerationListener,mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(proximityListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        //start classification
        long time_between_calculations = (long) (WINDOW_SIZE_SECONDS* OVERLAP * 1000);
        long time_to_first_calculation = (long) (START_DELAY_SECONDS * 1000);
        classificationFuture = mScheduler.scheduleWithFixedDelay(classificationRunnable, time_to_first_calculation, time_between_calculations, TimeUnit.MILLISECONDS);
        Log.d(CLASSIFICATION_DEBUG, "Classification started");
    }

    Runnable classificationRunnable = new Runnable() {
        @Override
        public void run(){
            try{
                run_aux();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run_aux() {
            int startTime = (int) System.currentTimeMillis();
            //get list of events
            List<SensorEventRecord> eventsInWindow = getLinesForWindow(new ArrayList<>(mSensorEventList));

            //Log.d("stuff", String.valueOf(eventsInWindow.size()));
            Window window = Window.createFromEventRecordList(eventsInWindow,"unknown");

            //calculate the features
            window.calculateAllFeatures();

            //create Instances-object
            Instances unlabeled = WekaUtil.getInstances(window, ClassifierType.EVENT_SNIFFER);
            //insert DenseInstance into Instances-object
            unlabeled.add(WekaUtil.convertWindowToInstance(window, unlabeled));

            //classify the window
            try {
                Log.d(CLASSIFICATION_DEBUG, "Size of sensoreventlist = " + mSensorEventList.size());
                if(eventClassifier == null){
                    Log.d(TAG, "Classifier = null");
                }
                String label_sitstand_string;
                String label_event_string;
                double[] dist_sitstand;
                double label = eventClassifier.classifyInstance(unlabeled.firstInstance());
                double label_sitstand;
                double[] dist_event = eventClassifier.distributionForInstance(unlabeled.firstInstance());
                unlabeled.firstInstance().setClassValue(label);
                /*double tap = unlabeled.firstInstance().value(unlabeled.classIndex()-4);
                Log.d("Number Of Taps", String.valueOf(tap));

                if(tap> 0 && tap < 4){
                    //Tap detected!
                    if(getTimeSinceLastTap() > MIN_TIME_BETWEEN_TAPS_IN_MILLIS) {
                        //acknowledgeTap();
                        //correctWindowUsingInference();
                    } else {
                        Log.d("Number Of Taps", "Tap rejected. Was too close to other tap!");
                    }
                }*/

                //prepare for the next classification
                unlabeled = WekaUtil.getInstances(window, ClassifierType.SIT_STAND_CLASSIFIER);
                unlabeled.add(WekaUtil.convertWindowToInstance(window, unlabeled));
                label_sitstand = standsitClassifier.classifyInstance(unlabeled.firstInstance());
                dist_sitstand = standsitClassifier.distributionForInstance(unlabeled.firstInstance());

                label_event_string = Classification.eventClassificationFromLabel(label);
                label_sitstand_string = Classification.sitStandClassificationFromLabel(label_sitstand);

                //Copy the important parts into a new window so serialization works
                String windowLabel = (label == 1)? label_event_string : label_sitstand_string;
                window.setLabel(windowLabel);
                Window saved_window = new Window(window);
                Classification classification = new Classification(label_event_string, label_sitstand_string, dist_event, dist_sitstand);

                if(label == 1){
                    //null event. Do nothing
                    event_in_last_cycle = false;
                    //To eliminate event shared between two windows gets duplicated
                } else if(noOrDifferentEventInLastCycle(label_sitstand_string)) {
                    if(label_sitstand == 1){
                        //presumably stand
                        Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
                        Date now = cal.getTime();
                        chart_events.add(new Classification(label_event_string, label_sitstand_string, dist_event, dist_sitstand, now));
                        updateBarChart();
                        communicateClassificationToUser(ClassifierPrediction.STAND);
                    } else {
                        //presumably sit
                        communicateClassificationToUser(ClassifierPrediction.SIT);
                    }

                    if(latest_events.size() > 1){
                        latest_events.poll();
                        latest_events.add(classification);
                    } else {
                        latest_events.add(classification);
                    }
                    event_in_last_cycle = true;
                } else {
                    event_in_last_cycle = false;
                }


                Log.d("clsfic", classification.toString());
                //Save recent windows in queue so correction is easier.
                if(recentWindows.size()> SIZE_OF_RECENT_WINDOWS){
                    recentWindows.poll();
                    recentWindows.add(new Pair<>(saved_window, classification));
                } else {
                    recentWindows.add(new Pair<>(saved_window, classification));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int time2 = (int) System.currentTimeMillis();
            Log.d("stuff", "Classifying done. Took " + (time2 - startTime));
        }
    };

    private boolean noOrDifferentEventInLastCycle(String label_sitstand_string) {
        String last_event_classification = "";
        if(latest_events.size()> 1){
            last_event_classification = (new LinkedList<>(latest_events).get(1)).getEvent_classification();
        }
        return !event_in_last_cycle || last_event_classification.equals( label_sitstand_string);
    }

    private void correctWindowUsingInference() {
        //find number of events
        int number_of_events = 0;
        for (Pair<Window, Classification> pair : recentWindows) {
            if (pair.getValue().getEvent_classification().equals(Classification.EVENT_CLASSIFICATION))
                number_of_events++;
        }

        if(number_of_events == 0){
            //find most probable event window
            Pair<Window, Classification> max_prob_pair = findMostProbableEventWindow();
            //correct to what it deems most probable
            handleSITorSTANDcase(max_prob_pair);
        } else if(number_of_events == 1){
            String latest_verified_stst_label = "";
            //stst-label for last verified event
            if(latest_events.size() < 2){
                //worthless
            } else {
                latest_verified_stst_label = latest_events.peek().getSitstand_classification();
            }
            Pair<Window, Classification> latest_event = findLastEventWindow();
            //find null-windows with different sit/stand-label than latest verified event-window with. select the one with highest probability of being an event
            Pair<Window, Classification> pair_save = findMostProbableNullWindow(latest_verified_stst_label);

            //if null window probability is > 25%
            if(pair_save != null &&pair_save.getValue().getEvent_distribution()[0] > 0.25){
                handleSITorSTANDcase(pair_save);
            } else
                handleNULLorWRONGcase(latest_event);

        } else {
            //find event-probability of last event-window
            Pair<Window, Classification> latest_event = findLastEventWindow();
            handleNULLorWRONGcase(latest_event);
        }

        updateBarChart();

        //clear latest event window becuase at this point nothing is verified
        latest_events = new LinkedBlockingQueue<>();
    }

    private void handleSITorSTANDcase(Pair<Window, Classification> max_prob_pair) {
        if(max_prob_pair.getValue().getSitstand_classification().equals(Classification.STAND_CLASSIFICATION)){
            Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
            Date now = cal.getTime();
            chart_events.add(new Classification(Classification.EVENT_CLASSIFICATION, Classification.STAND_CLASSIFICATION, new double[]{1, 0}, new double[]{1,0}, now));
        }
        Correction correction = new Correction(Classification.NULL_CLASSIFICATION,"", Classification.EVENT_CLASSIFICATION, max_prob_pair.getValue().getSitstand_classification());
        ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(max_prob_pair.getKey(), correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
    }

    private void handleNULLorWRONGcase(Pair<Window, Classification> latest_event) {
        if ( latest_event != null && latest_event.getValue().getEvent_distribution()[0] > 0.75){
            String corrected_sit_stand = (latest_event.getValue().getSitstand_classification().equals(Classification.SIT_CLASSIFICATION))? Classification.STAND_CLASSIFICATION : Classification.SIT_CLASSIFICATION;
            if(corrected_sit_stand.equals(Classification.STAND_CLASSIFICATION)){
                Calendar cal = Calendar.getInstance(getResources().getConfiguration().locale);
                Date now = cal.getTime();
                chart_events.add(new Classification(Classification.EVENT_CLASSIFICATION, Classification.STAND_CLASSIFICATION, new double[]{1, 0}, new double[]{1,0}, now));
            }
            Correction correction = new Correction(Classification.EVENT_CLASSIFICATION, latest_event.getValue().getSitstand_classification(), Classification.EVENT_CLASSIFICATION,corrected_sit_stand);
            ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(latest_event.getKey(), correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
        } else {
            Correction correction = new Correction(Classification.EVENT_CLASSIFICATION, "", Classification.NULL_CLASSIFICATION, "");
            Log.d("chart", "removed stand");
            chart_events.remove(chart_events.size()-1);
            ParseUtil.sendMostLikelyWindowToParseServerWithRichCorrection(latest_event.getKey(), correction, biasConfiguration.toString(), CLASSIFIER_OBJECT_ID);
        }
    }

    private Pair<Window, Classification> findMostProbableNullWindow(String latest_verified_stst_label) {
        Pair<Window, Classification> pair_save = null;
        if(!latest_verified_stst_label.equals("")){
            double max_prob = Double.MIN_VALUE;
            for (Pair<Window, Classification> pair : recentWindows) {
                Classification classification = pair.getValue();
                if( classification.getEvent_classification().equals(Classification.NULL_CLASSIFICATION)
                        && classification.getEvent_classification().contains(latest_verified_stst_label)
                        && classification.getEvent_distribution()[0] > max_prob){
                    pair_save = pair;
                }
            }
        }
        return pair_save;
    }

    private Pair<Window, Classification> findLastEventWindow() {
        Iterator<Pair<Window, Classification>> iter = recentWindows.iterator();
        Pair<Window, Classification> latest_event = null;
        while(iter.hasNext()){
            Pair<Window,Classification> temp =iter.next();
            if( temp.getValue().getEvent_classification().equals(Classification.EVENT_CLASSIFICATION))
                latest_event = temp;
        }
        return latest_event;
    }

    private Pair<Window, Classification> findMostProbableEventWindow() {
        Pair<Window,Classification> max_prob_pair = null;
        double max_prob = Double.MIN_VALUE;
        for (Pair<Window, Classification> pair : recentWindows) {
            double probability_for_window = pair.getValue().getEvent_distribution()[0];
            if(probability_for_window > max_prob){
                max_prob = probability_for_window;
                max_prob_pair = pair;
            }
        }
        return max_prob_pair;
    }

    private long getTimeSinceLastTap() {
        long current_time = System.currentTimeMillis();
        return (current_time - time_of_last_tap);
    }

    private void acknowledgeTap() {
        time_of_last_tap  = System.currentTimeMillis();
        if(currentGlobalSoundSetting == AudioManager.RINGER_MODE_SILENT){
            //do thing
        } else {
            if(toggleSoundStatus){
                playSound(R.raw.tapdetected);
            } else {
                long[] pattern = {0, 2000, 500, 1000};
                mVibrator.vibrate(pattern, -1);
            }
        }
    }

    private void communicateClassificationToUser(ClassifierPrediction classification){
        if( currentGlobalSoundSetting == AudioManager.RINGER_MODE_SILENT){
            //do nothing
        } else {
            if (classification == ClassifierPrediction.NULL){
                //do nothing
            } else if (classification == ClassifierPrediction.SIT){
                if(toggleSoundStatus){
                    playSound(R.raw.sitdowndetected);
                } else {
                    mVibrator.vibrate(SIT_PATTERN, -1);
                }
            } else if (classification == ClassifierPrediction.STAND){
                if(toggleSoundStatus){
                    playSound(R.raw.standupdetected);
                } else {
                    mVibrator.vibrate(STAND_PATTERN, -1);
                }
            }
        }
    }

    private void streamClassificationToServer(ClassificationEventRecord classificationEventRecord) {
        String json = classificationEventRecordJsonAdapter.toJson(classificationEventRecord);
        try {
            mDos.writeUTF(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playSound(int uri){
        float volume = 1;
        soundPool.play(soundPoolMap.get(uri), volume, volume, 1, 0 , 1f);
    }


    public void playSoundMisc(int uri){
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), uri);
        //mp.setAudioStreamType(AudioManager.
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("Playback", "Error! : " + what + " Extra : " + extra);
                return true;
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    private List<SensorEventRecord> getLinesForWindow(List<SensorEventRecord> sensorEventList) {
        //get time of last line
        int window_size_nanoseconds = (int) (WINDOW_SIZE_SECONDS * 1000000000);
        SensorEventRecord lastLine = sensorEventList.get(sensorEventList.size()-1);
        //get all lines up to [window_size_in_seconds] seconds before that
        for (int i = sensorEventList.size()-1; i > 0; i--) {
            SensorEventRecord line = sensorEventList.get(i);
            if ((lastLine.getTimestamp()- line.getTimestamp()) > window_size_nanoseconds){
                return new ArrayList<>(sensorEventList.subList(i+1,sensorEventList.size()-1));
            }
        }
        return new ArrayList<>(sensorEventList);
    }

    SensorEventListener gravityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            latestGravityEvent = event.values.clone();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener proximityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            latestProximityEvent = event.values.clone();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener accelerationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (latestGravityEvent != null && latestProximityEvent != null) {
                SensorEventRecord record = new SensorEventRecord(event.timestamp, latestProximityEvent, latestGravityEvent, event.values.clone());
                record.applyBias(biasConfiguration);
                mSensorEventList.add(record);
                if(mSensorEventList.size() > 5000)
                    mSensorEventList.remove(0);
                if(stream_to_server){
                    streamRecordToServer(record);
                }
            }
        }

        private void updateTextViews(SensorEventRecord event) {
            mTextViewAccX.setText("Acc X = "+decimalFormat.format(event.getAcceleration()[0]));
            mTextViewAccY.setText("Acc Y = "+decimalFormat.format(event.getAcceleration()[1]));
            mTextViewAccZ.setText("Acc Z = "+decimalFormat.format(event.getAcceleration()[2]));

            mTextViewGraX.setText("Gra X = "+decimalFormat.format(event.getGravity()[0]));
            mTextViewGraY.setText("Gra Y = "+decimalFormat.format(event.getGravity()[1]));
            mTextViewGraZ.setText("Gra Z = "+decimalFormat.format(event.getGravity()[2]));

            AbstractMap.SimpleEntry<Double,Double> relativeMovement = CalculationUtil.calculateRelativeMovement(event);

            mTextViewAccUp.setText("Acc Up = "+decimalFormat.format(relativeMovement.getKey()));
            mTextViewAccRest.setText("Acc Rest = "+decimalFormat.format(relativeMovement.getValue()));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private boolean differenceBiggerThan15Seconds(long timestamp1, long timestamp2) {
        return Math.abs(timestamp2 - timestamp1) > 15000000000L;
    }

    private void streamRecordToServer(SensorEventRecord record) {
        String json = sensorEventRecordJsonAdapter.toJson(record);
        try {
            mDos.writeUTF(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateClassifiers(MenuItem item) {
        updateClassifiers();
    }

    public void configBias(MenuItem item) {
        stopClassification();
        unregisterReceiver(mRingermodeReceiver);
        Intent configuration_intent = new Intent(this, ConfigurationActivity.class);
        startActivity(configuration_intent);
    }

    public void editUserGroupName(MenuItem item) {
        openUsernameFragment();
    }

    private void openUsernameFragment() {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("fragment_username");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }

        UsernameDialogFragment editNameGroupDialog = new UsernameDialogFragment();
        editNameGroupDialog.show(manager, "fragment_username");
    }

    @Override
    public void onFinishDialog(final String user, final String group) {
        username = user;
        groupname = group;
        SharedPreferences.Editor editor = getSharedPreferences(USER_GROUP_NAME, 0).edit();
        editor.putString("User_name", user);
        editor.putString("Group_name", group);
        editor.apply();
        Toast.makeText(getApplicationContext(), "User and group name updated", Toast.LENGTH_SHORT).show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ClassifierConfiguration");
        Log.d("debug", "User = " + user + " Group = " + group);
        query.getInBackground(CLASSIFIER_OBJECT_ID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e != null) {
                    Log.d("debug", e.toString());
                }
                parseObject.put("Group", group);
                parseObject.put("Username", user);
                parseObject.saveInBackground();
            }
        });
    }

    public void toggleSound(MenuItem item) {
        toggleSoundStatus = !toggleSoundStatus;
        SharedPreferences.Editor editor = getSharedPreferences(USER_SOUND_TOGGLE_STATUS, 0).edit();
        editor.putBoolean("soundtogglestatus", toggleSoundStatus);
        editor.apply();
        String toogleStatus = toggleSoundStatus? "on" : "off";
        Toast.makeText(getApplicationContext(), "Sound is now "+ toogleStatus, Toast.LENGTH_SHORT).show();
    }
}
