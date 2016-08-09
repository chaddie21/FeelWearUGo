package com.example.chadwick.feelwearugo;


import android.annotation.TargetApi;
import android.app.Activity;
import java.lang.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// for the text to speech interface


public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    public MainActivity(){

    }

    public class OnDoubleTapListener implements View.OnTouchListener {


    /*

    Usage:

      myView.setOnTouchListener(new OnDoubleTapListener(this) {
        @Override
        public void onDoubleTap(MotionEvent e) {
          Toast.makeText(MainActivity.this, "Double Tap", Toast.LENGTH_SHORT).show();
        }
      });

    */

        private GestureDetector gestureDetector;

        public OnDoubleTapListener(Context c) {
            gestureDetector = new GestureDetector(c, new GestureListener());
        }

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                OnDoubleTapListener.this.onDoubleTap(e);
                return super.onDoubleTap(e);
            }
        }

        public void onDoubleTap(MotionEvent e) {
            // To be overridden when implementing listener
        }
    }
    public class OnSwipeTouchListener implements View.OnTouchListener {

    /*

Usage:

  myView.setOnTouchListener(new OnSwipeTouchListener(this) {
    @Override
    public void onSwipeDown() {
      Toast.makeText(MainActivity.this, "Down", Toast.LENGTH_SHORT).show();
    }
  }

*/

        private GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context c) {
            gestureDetector = new GestureDetector(c, new GestureListener());
        }

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            // Determines the fling velocity and then fires the appropriate swipe event accordingly
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeDown();
                            } else {
                                onSwipeUp();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeUp() {
        }

        public void onSwipeDown()
        {
        }
    }
    private final String URL_TO_HIT = "http://104.155.128.82";
    private String URL_TO_HIT2 = "http://104.155.128.82/ids/?";
    private final static String TAG = "Main-Activity";
    private static final boolean D = true;


    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_SPEECH_INPUT2= 101;
    private final int REQ_CODE_SPEECH_INPUT3= 102;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BroadcastReceiver receiver;
    private Button speakButton;
    public String spokenWords;
    private PedometerSettings mPedometerSettings;
    private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
    private float mSpeedValue;
    private boolean mIsRunning;
   //TEXT TO SPEECH
    public TextToSpeech ttObj;
    private View txtSpeechInput;
    public static Intent sendSpokenWords;

    //----------------------------Singleton pattern instantiation-----------------------------------
           UWIMap uwiMap = UWIMap.getUWIMap();
    //----------------------------Broadcast receiver -----------------------------------------------
    double latitude,longitude, finalLatitude, finalLongitude;
    float accuracy, finalAccuracy;
    boolean isPlace = false;
    boolean isBusTerminal = false;
    boolean isRestRoom = false;
    boolean isIntersection = false;

    //-----------------------------Bluetooth Connection---------------------------------------------
    private BluetoothAdapter bluetoothAdapter = null;
    private static BluetoothCommunication btComm;
    private static Set<BluetoothDevice> newDevices;
    private Set<BluetoothDevice> pairedDevices;

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final boolean DEBUG = true;



    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        speakButton = (Button) findViewById(R.id.speakButton);

        //---------------PATHFINDER----------------------------------
//            uwiMap.setStartVertexViaName("ped1");
//
//            /*if (!uwiMap.atANodeOnMap){
//                uwiMap.addCurrentLocationToMapAndSetAsStart();
//            }else{
//                uwiMap.getCurrentNodeAndSetItAsStart();
//            }*/
//
//            uwiMap.setEndVertexViaName("undercroft");
//            PathFinder pF = new PathFinder();
//            Log.d("pathText", "HERE!");
//         TODO:   pF.pathFinder_setNodeBearing();

            //float[] b=pF.getPathBearing();
            //float[] nt = pF.setNextTurnAngles(b);
            //pF.setNextTurnInstructions(nt);
            // String[] turn = pF.getTurn();
            //Log.d("pathTest:", pF.getPath().toString());
            //Log.d("pathTest1", nt[0]+"::"+nt[1]+"::"+nt[2]+"");
            //for(int i = 0; i< turn.length; i++) Log.d("pathTest2", turn[i]+"");

        //----------------------HTTPConnection to get path-----------------------------------
            // TODO: Call HTTPConnect
            // TODO: create nodes & add to path list
            // TODO: get bearing to next nodes

        //-----------TEXT TO SPEECH: Creating a new TTS Object--------

            ttObj = new TextToSpeech(this, this);
            sendSpokenWords = new Intent();
            sendSpokenWords.setAction("android.intent.action.RECEIVE");

        //-------------Speak Button---------------------------

            speakButton.setOnTouchListener(
                    new OnSwipeTouchListener(this) {
                        @Override
                        public void onSwipeUp() {
                            speechInput();
                            Log.i(TAG, "Upward Swipe");
                        }

                        public void onSwipeDown() {
                            speakString("So user, where would you like to go?");
                            Log.i(TAG, "downward Swipe");
                            //Todo: make http Request
                            //new ConnectionTask().execute(URL_TO_HIT);
                        }

                        public void onSwipeLeft() {
                            URL_TO_HIT2 = "http://104.155.128.82/ids/?";
                            speakString("source node ID");
                            //URL_TO_HIT2 = URL_TO_HIT2+"a=100"+"&";
                            speechInput();
                            //TODO:
//                            Intent sendText = new Intent(Intent.ACTION_VIEW);

//                            try {
//                                SmsManager smsManager = SmsManager.getDefault();
//                                smsManager.sendTextMessage("4735768", null, "Reach", null, null);
//                                speakString("message sent");
//                                Log.i(TAG, "sms failed");
//                                Toast.makeText(getApplicationContext(), "SMS SENT", Toast.LENGTH_LONG).show();
//                            } catch (Exception e) {
//                                Toast.makeText(getApplicationContext(), "SMS FAILED!", Toast.LENGTH_LONG).show();
//                                Log.i(TAG, "sms failed");
//                                speakString("message failed");
//                                e.printStackTrace();
//                            }
                        }
                        public void onSwipeRight(){
                            //URL_TO_HIT2 += "b=96";
                            //new ConnectionTask().execute(URL_TO_HIT2);
                            speakString("target node ID");
                            speechInput2();
                        }
                    }
            );

    //--------------------------Localization Service --------------------------------



    //--------------------------pedometer Initialization-----------------------------
//        mPedometerSettings = new PedometerSettings("m",0.762f,6.0f,true,true);
//        mStepValue =0;
//        mPaceValue =0;

        //-------------------------------------------------------------------------------
        //TODO: download 3km radius map if internet connection
        //TODO: search for nearest node in file
        //TODO: if
        // ---------------------------------------------------------------------------

        btComm = new BluetoothCommunication(this, mHandler);

    }

    @Override
    public void onStart(){
        super.onStart();

        Log.i(TAG, "Entered the Start Mode");
        try{
            startLocalizationService();

        }catch(RuntimeException e){

        }
        try {
            //startStepService();
        }catch(RuntimeException e){

        }

    }

    @Override
    public void onRestart(){
        super.onRestart();

        Log.i(TAG, "The Activity is in restart() mode");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");




        //TODO Setting up Broadcast Receiver

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String latitude = intent.getStringExtra("Latitude");
                String longitude = intent.getStringExtra("Longitude");
                String provider = intent.getStringExtra("Provider");
                Log.i(TAG, "Entered onResume mode: Broadcasting the location info");
            }
        };
        this.registerReceiver(receiver, intentFilter);

        //------------------- pedometer---------------------

       try{
            if (mIsRunning){
                bindStepService();
            }
       }catch(RuntimeException e){
            e.printStackTrace();
       }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
     }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause(){
        super.onPause();

        this.unregisterReceiver(receiver);

        try {
             if (mIsRunning) unbindStepService();

        }catch(RuntimeException e){
            Log.i(TAG, "Runtime Error");
        }
        Log.i(TAG, "Entered onPause() mode");
        //savePaceSetting();
    }

    @Override
    public void onStop(){
        super.onStop();

        if (ttObj != null){
            ttObj.stop();
        }

        Log.i(TAG, "Entered onStop() mode");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(ttObj != null){
            ttObj.shutdown();
        }
        this.unregisterReceiver(mReceiver);
        Log.i(TAG, "Activity has been shutdown");
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = ttObj.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Not Supported");
            }
            else{
                speakButton.setEnabled(true);

            }
        }else{
            Log.e("TTS", "Initialization Failed");
        }
    }

//    public void enableBluetooth(){
//        if(!bluetoothAdapter.isEnabled()) {
//            bluetoothAdapter.enable();
//            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
//        }
//    }


    //Replacement for onItemClick bluetooth devices dialog
//    private void streetConnectDeviceConnection(Set<BluetoothDevice> devices){
//        bluetoothAdapter.cancelDiscovery();
//        for (BluetoothDevice device: devices){
//            //To ensure that only address of the Street Connect Device
//            if(device.getName().equals("")){
//                Intent intent = new Intent();
//                intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
//                setResult(Activity.RESULT_OK, intent);
//            }else{
//                setResult(Activity.RESULT_CANCELED);
//            }
//        }
//        finish();
//    }

//    private void doDiscovery(){
//        if (DEBUG) Log.d(TAG, "doDiscovery()");
//        if(bluetoothAdapter.isDiscovering()){
//            bluetoothAdapter.cancelDiscovery();
//        }
//        bluetoothAdapter.startDiscovery();
//    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    newDevices.add(device);
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

            }
        }
    };

    public void connectService(){
        try{
            makeToast("Bluetooth connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter.isEnabled()){
                btComm.start();
                btComm.connectDevice("HC-06");
                Log.d(TAG, "BTService started - listening");
                makeToast("Bluetooth connected");
            }
            else{
                Log.w(TAG, "BtService started - bluetooth is not enabled");
                makeToast("Bluetooth not enabled");
            }
        }catch (Exception e){
            Log.e(TAG, "Unable to start btComm", e);
            makeToast("Unable to connect to Bluetooth "+ e);
        }
    }


    private final Handler mHandlerBT = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MESSAGE_STATE_CHANGE: {
                    if (DEBUG) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothCommunication.STATE_CONNECTED:
                            break;
                        case BluetoothCommunication.STATE_CONNECTING:
                            //TODO:
                            break;
                        case BluetoothCommunication.STATE_LISTEN:
                            break;
                        case BluetoothCommunication.STATE_NONE:
                            break;
                    }
                    break;
                }

                case MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ ");
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME "+msg);
                    break;
                case MESSAGE_TOAST: {
                    Log.d(TAG, "MESSAGE_TOAST " + msg);
                    makeToast(msg.getData().getString(TOAST));
                    break;
                }
            }
        }
    };







    public void speechInput(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //identifying your application by calling package
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try{
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }catch(ActivityNotFoundException a){
            Log.i(TAG, "Speech Not Supported");
        }

    }

    public void speechInput2(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //identifying your application by calling package
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try{
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT2);
        }catch(ActivityNotFoundException a){
            Log.i(TAG, "Speech Not Supported");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //final int REQ_CODE_SEND_SPEECH_STRING = 10;

        switch (requestCode) {

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    spokenWords = result.get(0);
                    speakString(spokenWords);
                    speakString("did you say node ID" +spokenWords+". if not, please record again");
                    sendSpokenWords.putExtra("SpokenWords",spokenWords);
                    sendBroadcast(sendSpokenWords);
                    URL_TO_HIT2 = URL_TO_HIT2+"a="+spokenWords+"&";
                    Log.d("speech input 2", "");
                    //setEndNode(spokenWords);


//                    --------------PATHFINDER-----------------------------------
//                    String ped1 = "Student Services";
//                    /*try{
//                        uwiMap.setEndVertexViaName(spokenWords);
//                    }catch (NullPointerException n){
//                        speakString("Place Currently Not on the Map");
//                        Log.d("UWIMAP:","No such vertex exist");
//                    }
//                    PathFinder pF = new PathFinder();
//                    Log.d("pathText", "HERE!");
//                    Log.d("pathTest", pF.getPath().toString());
//                    */
//                    -----------------------------------------------------------

//                    /*runOnUiThread(new Runnable() {
//                        public void run()
//                        {
//                            Toast.makeText(getApplicationContext(),spokenWords+"?", Toast.LENGTH_SHORT).show();
//                        }
//                    });*/
                }
                break;
            }

            case REQ_CODE_SPEECH_INPUT2: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    spokenWords = result.get(0);
                    speakString("did you say target node ID" +spokenWords+". if not, please record again");
                    URL_TO_HIT2 += "b="+spokenWords;
                    new ConnectionTask().execute(URL_TO_HIT2);
                    //setStartNode(spokenWords);
                    Log.d("speech input 2", "");
                }
                break;
            }
            case REQUEST_CONNECT_DEVICE:{
                if(requestCode == Activity.RESULT_OK){
                    String address = data.getExtras().getString(MainActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    btComm.connect(device);
                }
                break;
            }
            case REQUEST_ENABLE_BT:{
                break;
            }
        }
    }




    public void makeToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setEndNode(String nameOfNode){

        // KEYNOTE: Initialization of the variable uwiMap with static uwiMap;
        this.uwiMap = UWIMap.getUWIMap();
        try {
            this.uwiMap.setEndVertexViaName(nameOfNode);
        }catch (NullPointerException e){Log.d("set End Node Via Name:", "THROWS NULL POINTER EXCEPTION");}
        catch (IndexOutOfBoundsException e){Log.d("set End Node Via Name:", "THROWS INDEX OUT OF BOUND EXCEPTION");}
    }

    private void setStartNode(String name){
        // KEYNOTE: Initialization of the variable uwiMap with static uwiMap;
        this.uwiMap = UWIMap.getUWIMap();
        try {
            this.uwiMap.setStartVertexViaName(name);
        }catch (NullPointerException e){Log.d("set Start Node", "THROWS ");}
        catch (IndexOutOfBoundsException e){Log.d("Via Name:", "THROWS");}
    }

    public void checkVoiceRecognition(){
        // Check if Voice Recognition is Enabled
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if (activities.size()==0){
            //speakButton.setEnabled(false);
            Log.i(TAG, " Voice Recognition not Activated");

        }
    }


    @SuppressWarnings("deprecations")
    public void speakString( String string) {
        ttObj.speak(string,TextToSpeech.QUEUE_FLUSH,null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakString21( CharSequence string) {
        ttObj.speak(string, TextToSpeech.QUEUE_FLUSH,null,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    //------------------------------------ pedometer methods-------------------------------------------------------------

        private void savePaceSetting() {
     //TODO implement this method
     }

    //----------------------------------- Starting Background Threads-----------------------------------------------------

        private void doStartStepService() {
            mIsRunning = true;
            startService(new Intent(this,
                    StepService.class));
        }

        private void startStepService(){
            Thread StepServiceThread = new Thread(null,backgroundStepService, "StepService_Background");
            StepServiceThread.start();
        }

        private Runnable backgroundStepService = new Runnable() {
            @Override
            public void run() {
                doStartStepService();
            }
        };


        private void doStartLocalizationService(){
            startService(new Intent (this, LocalizationService.class));
        }

        private void startLocalizationService(){
            Thread localizationServiceThread = new Thread(null,backgroundLocalizationService, "StepService_Background" );
            localizationServiceThread.start();
        }

        Runnable backgroundLocalizationService = new Runnable() {
            @Override
            public void run() {
                doStartLocalizationService();
            }
        };
 

    // ---------------------------------------------------------------------------------------------------------------------


    private void bindStepService() {
        bindService(new Intent(this, StepService.class),
                    mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStepService(){
        unbindService(mConnection);
    }

    private void stopStepStepService(){
        mIsRunning =false;
        if(mService != null){
            stopService(new Intent(MainActivity.this, StepService.class));
        }
    }

    private void resetValues(boolean updateDisplay){
        if (mService!= null && mIsRunning){
            mService.resetValues();
        }
    }

    private StepService mService;

    private ServiceConnection mConnection  = new ServiceConnection() {

        // Handles the service binding
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService =((StepService.StepBinder)service).getService();

            mService.registerCallBack(mCallBack);
            mService.reloadSettings();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private StepService.ICallback mCallBack = new StepService.ICallback(){

        @Override
        public void stepsChanged(int value){
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }

        @Override
        public void paceChanged(int value) {

        }

        @Override
        public void distanceChanged(float value) {

        }

        @Override
        public void speedChanged(float value) {

        }
    };


    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;


    private class MyReceiver extends BroadcastReceiver {
        Intent intent;
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO: change to double
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
            accuracy = intent.getFloatExtra("accuracy", 0);
        }
    }



    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch(msg.what){

                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(),mStepValue+"",Toast.LENGTH_SHORT).show();
                        }
                    });



                     break;


                case PACE_MSG:

                    mPaceValue = msg.arg1;

                    if(mPaceValue  <= 0){
                       // Toast.makeText(getApplicationContext(),'0',Toast.LENGTH_SHORT).show();
                    }else{
                        runOnUiThread(new Runnable() {
                            public void run()
                            {
                                Toast.makeText(getApplicationContext(),mPaceValue+"",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    break;


                case DISTANCE_MSG:

                    mDistanceValue = (msg.arg1)/1000f;
                    final Handler handler2 = new Handler();

                    if (mDistanceValue<= 0){
                        //Toast.makeText(getApplicationContext(),"Distance travelled: 0",Toast.LENGTH_SHORT).show();
                    }else {

                        runOnUiThread(new Runnable() {
                            public void run()
                            {
                                Toast.makeText(getApplicationContext(),(""+ (mDistanceValue + 0.000001f)).substring(0,5),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;

                case SPEED_MSG:

                    mSpeedValue = (msg.arg1)/1000f;

                    if(mSpeedValue<=0); //Toast.makeText(getApplicationContext(),"Speed: 0",Toast.LENGTH_SHORT).show();

                    else {
                         runOnUiThread(new Runnable() {
                                    public void run()
                                    {
                                        Toast.makeText(getApplicationContext(),(""+ (mSpeedValue + 0.000001f)).substring(0,5)
                                                ,Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };



}