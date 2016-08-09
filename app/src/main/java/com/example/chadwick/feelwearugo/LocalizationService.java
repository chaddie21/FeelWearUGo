package com.example.chadwick.feelwearugo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class LocalizationService extends Service implements TextToSpeech.OnInitListener {


    public LocalizationService() {}

    //---------------- Misc Variable declaration ---------------------------------------------------

        private static final int FENCE_THRESHOLD = 5;
        private static final int TEN_SECONDS = 1000 *10;
        private LocationManager locationManager;
        private MyLocationListener listener;
        private Location previousBestLocation = null;
        Intent intent;
        int count =0;
        public TextToSpeech tts;

    //----------------------- Singleton pattern instantiation;--------------------------------------

        UWIMap uwiMap = UWIMap.getUWIMap();
        Location location1 = new Location("Me");

    //------------------------- Receiving Broadcasts------------------------------------------------

        private BroadcastReceiver receiver;
        boolean hasReceivedSpokenWord = false;




    @Override
    public void onCreate(){
        super.onCreate();
        tts = new TextToSpeech(this, this);
        intent = new Intent("android.intent.action.MAIN");

        location1.setLatitude(18.005530);
        location1.setLongitude(-76.748454);



        registerReceiver(receiver, new IntentFilter("android.intent.action.RECEIVE"));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                4000,0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                4000,0, listener);
        Log.i("Receiver:", "Received SpokenWords from Main Activity");



        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String spokenWords = intent.getStringExtra("SpokenWords");
                hasReceivedSpokenWord = true;
                Log.d("Receiver","Got Message:"+spokenWords);
            }
        };



        //TODO: Setting up a local broadcast manager
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation){
        if(currentBestLocation == null){
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TEN_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -TEN_SECONDS;
        boolean isNewer = timeDelta > 0;

        if(isSignificantlyNewer){
            return  true;
        }else if(isSignificantlyOlder) {
            return false;
        }
        // Checking the accuracy of the newly Provided Location Data
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta >0;
        boolean isMoreAccurate = accuracyDelta <0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Checking if old and New is from the same Provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determining the quality of Location Data

        if (isMoreAccurate) return true;

        else if(isNewer && !isLessAccurate)return true;

        else if(isNewer &&!isSignificantlyLessAccurate &&
                isFromSameProvider)return true;

    return false;
    }

    //Checks Whether two provider are the same
    private boolean isSameProvider(String provider1,String provider2){
        if(provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void onDestroy(){

        super.onDestroy();
        unregisterReceiver(receiver);
        Log.v("STOP SERVICE", "DONE!");
        locationManager.removeUpdates(listener);
        count =0;
    }

    public static Thread performOnBackgroundThread(final Runnable runnable){
        final Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }finally {
                    Log.v("finally","Throwable");
                }
            }
        };
        return thread;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Not Supported");
            }
            else{
             }
        }else{
            Log.e("TTS", "Initialization Failed");
        }
    }

    @SuppressWarnings("deprecations")
    public void speakString(String string) {
        tts.speak(string,TextToSpeech.QUEUE_FLUSH,null);
    }

    public class MyLocationListener implements LocationListener{

        private static final String TAG = "LOC_CHANGE_TAG";

        public void onLocationChanged(final Location loc) {
            final Handler handler = new Handler();
            final String SEND_LOCATION = "SEND_LOCATION";
            Intent intent = new Intent(SEND_LOCATION);


            if (isBetterLocation(loc, previousBestLocation)) {

                //--------- Setting user location and Heading of the UWIMap-------------------------
                    uwiMap.setLocationOnMap(loc);
                    uwiMap.setHeadingOnMap(loc.getBearing());
                //----------------------------------------------------------------------------------

                speakString(getNodeNotification(loc));
                speakString(isTooFast(loc.getSpeed()));

                intent.putExtra("latitude", loc.getLatitude());
                intent.putExtra("longitude", loc.getLongitude());
                intent.putExtra("accuracy", loc.getAccuracy());
                sendBroadcast(intent);



//                final Runnable updateGUI = new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "latitude:" + loc.getLatitude()
//                                + " Longitude:" + loc.getLongitude() + " Provider:"
//                                + loc.getProvider()+ (loc.hasBearing()? " Bearing:"+loc.getBearing():"")
//                                +(loc.hasSpeed()? "Speed:"+loc.getSpeed()+"m/s":"")+"Accuracy:"+loc.getAccuracy()+""+getNodeNotification(loc)+"", Toast.LENGTH_SHORT).show();
//                    }
//                };
//
//                final Runnable updateToast = new Runnable() {
//                    @Override
//                    public void run() {
//                        handler.post(updateGUI);
//                    }
//                };
//
//                Thread thread = new Thread(null, updateToast, "Toast_background");
//                thread.start();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {

                final Handler handler = new Handler();

                final Runnable updateGUI = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Gps Enabled", Toast.LENGTH_SHORT).show();
                    }


                };

                final Runnable updateToast = new Runnable() {
                        @Override
                        public void run() {
                            handler.post(updateGUI);
                        }
                };

                Thread thread = new Thread(null, updateToast, "Toast_background");
                thread.start();


        }

        @Override
        public void onProviderDisabled(String provider) {
            final Handler handler = new Handler();

            final Runnable updateGUI = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext()
                            ,"GPS Disabled", Toast.LENGTH_SHORT).show();
                }


            };

            final Runnable updateToast = new Runnable() {
                @Override
                public void run() {
                    handler.post(updateGUI);
                }
            };

            Thread thread = new Thread(null, updateToast, "Toast_background");
            thread.start();
        }

        private String getNodeNotification(Location loc){
            int aLittleMore = 6;
            ArrayList<Node> vertexList = new ArrayList<>();
            String notification = null;
            vertexList.addAll(uwiMap.getGraph().vertexSet());

            for (int i = 0; i < vertexList.size(); i++)
            {
                if((loc.distanceTo(vertexList.get(i)))<=(FENCE_THRESHOLD + aLittleMore)){

                    uwiMap.setAtANodeOnMap(true);
                    uwiMap.setCurrentNode(vertexList.get(i));

                    if (loc.hasBearing()) {
                        float buffer = loc.getBearing();
                        if(buffer>180.00) buffer -= 360.00f;
                        float bearing = (float) (((((vertexList.get(i).getHeading() - buffer) % 360.00) + 540.00) % 360.00) - 180.00);
                        //TODO: PREE!!
                        vertexList.get(i).setNextAction(bearing);
                    }

                    notification = vertexList.get(i).getNotification();

                    if(vertexList.get(i).isDestination()){

                        Intent sendText = new Intent(Intent.ACTION_VIEW);
                        if(!(vertexList.get(i).getContactNumber() == null)) {
                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage("4735768", null, "Reach", null, null);
                                speakString("message sent");
                                Log.i(TAG, "sms sent");
                                Toast.makeText(getApplicationContext(), "SMS SENT", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "SMS FAILED!", Toast.LENGTH_LONG).show();
                                Log.i(TAG, "sms failed");
                                speakString("message failed");
                                e.printStackTrace();
                            }
                        }
                        uwiMap.resetAllNodesNotification();
                    }
                }
                else{
                    uwiMap.setAtANodeOnMap(false);

                }
            }
            return notification;
        }

        private String isTooFast(float speed){
            return (speed >= 1.78 ? "You may be walking too fast. Slow down to reduce your chances of hitting on obstacles": "");
        }

        private String getAccuracyStatus(Location loc){
            String string ="";
            if (loc.hasAccuracy()){
                 if(loc.getAccuracy()>= 4){
                   string = "Not accurate enough" ;
                 }
                else{
                     string = "sufficient accuracy";
                 }
            }
            return string;
        }

    }

}




