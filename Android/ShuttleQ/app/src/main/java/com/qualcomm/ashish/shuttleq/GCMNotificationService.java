package com.qualcomm.ashish.shuttleq;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by ashish on 7/16/15.
 */
public class GCMNotificationService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private Location mLastLocation;
    private double latitude, longitude;
    private String url_add_location2;
    // session manager
    private SessionManager session;
    private String username;
    private String routename;
    private String Ipaddr;
    // JSON parser
    private JSONParser jsonParser=new JSONParser();
    public static final int notifyID = 9001;
   // private static final int LOCATION_INTERVAL = 1000;
  //  private static final float LOCATION_DISTANCE = 10f;

  /*  private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };*/
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;

    }
    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        // Initialise the Session manager
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user= session.getUserDetails();
        username=user.get(SessionManager.KEY_USERNAME);
        routename=user.get(SessionManager.KEY_ROUTE);
        Ipaddr=user.get(SessionManager.KEY_IP);
        url_add_location2 = "http://"+Ipaddr+"/android_connect/set_user_info2.php";
        sendNotification("hello");
        new AddLocation2().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        this.stopSelf();
    }
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
     /*  if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }*/
    }
    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

    }

    public HashMap<String,String> displayLocation() {
        HashMap<String, String> location=new HashMap<String, String>();
        try {
            //   mLocationManager.requestLocationUpdates(
            //           LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
            //           mLocationListeners[1]);

            // copy the same to GPS TODO
            mLastLocation=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                location.put("latitude", String.valueOf(latitude));
                location.put("longitude", String.valueOf(longitude));
                return location;
            }


        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            //   mLocationManager.requestLocationUpdates(
            //            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
            //           mLocationListeners[0]);
            mLastLocation=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                location.put("latitude", String.valueOf(latitude));
                location.put("longitude", String.valueOf(longitude));
                return location;
            }

        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        return location;
    }


    class AddLocation2 extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> location = displayLocation();
            String latitude = location.get("latitude");
            String longitude = location.get("longitude");

            List<NameValuePair> param = new ArrayList<NameValuePair>();
            if ((latitude != null) && (longitude != null)) {
                param.add(new BasicNameValuePair("latitude", latitude));
                param.add(new BasicNameValuePair("longitude", longitude));
                param.add(new BasicNameValuePair("username", username));
                param.add(new BasicNameValuePair("route", routename));

                Log.e(TAG, latitude + longitude + username + routename);
                JSONObject json = jsonParser.makeHttpRequest(url_add_location2, "POST", param);
                return "pass";
            }
            return "fail";
        }

        protected void onPostExecute(String result) {


        }
    }

   /* private void sendNotification(String msg) {
        Intent resultIntent = new Intent(this, SendLocation.class);
        resultIntent.putExtra("msg", msg);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("shuttleQ")
                .setContentText("Sending your location")
                .setSmallIcon(R.mipmap.ic_launcher);
        ;

        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setContentText("New message from Server");
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    } */
   private void sendNotification(String msg) {
       Intent resultIntent = new Intent(this, SendOrReceive.class);
       resultIntent.putExtra("msg", msg);
       PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
               resultIntent, PendingIntent.FLAG_NO_CREATE);

       NotificationCompat.Builder mNotifyBuilder;
       NotificationManager mNotificationManager;

       mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

       mNotifyBuilder = new NotificationCompat.Builder(this)
               .setContentTitle("shuttleQ")
               .setContentText("Sending your location")
               .setSmallIcon(R.mipmap.ic_launcher);
       ;

       // Set pending intent
       mNotifyBuilder.setContentIntent(resultPendingIntent);

       // Set Vibrate, Sound and Light
       int defaults = 0;
       defaults = defaults | Notification.DEFAULT_LIGHTS;
       defaults = defaults | Notification.DEFAULT_VIBRATE;
       defaults = defaults | Notification.DEFAULT_SOUND;

       mNotifyBuilder.setDefaults(defaults);
       // Set the content for Notification
       mNotifyBuilder.setContentText("Sending your location");
       // Set autocancel
       mNotifyBuilder.setAutoCancel(true);
       // Post a notification
       mNotificationManager.notify(notifyID, mNotifyBuilder.build());
   }

}
