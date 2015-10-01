package com.qualcomm.ashish.shuttleq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.concurrent.Executor;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReceiveLocation extends Activity {

    // Google Map
    private GoogleMap googleMap;
    // latitude and longitude
    private double latitude;   // set values here from database TODO
    private double longitude;
    ProgressDialog pDialog;
    private String Ipaddr;
    private String url_get_location;
    private final String TAG_SUCCESS = "success";
    private JSONParser jsonParser = new JSONParser();
    private SessionManager session;

    GetLocation2 getLoc2;
    private int startThreadCount=0;
    private int stopThreadCount=0;
    private String routename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_location);
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        routename = user.get(SessionManager.KEY_ROUTE);
        Ipaddr = user.get(SessionManager.KEY_IP);
        url_get_location = "http://"+Ipaddr+"/android_connect/get_user_info.php";
        try {

            //executing another thread in background, as an asynchronous thread update_location might be running

            new GetLocation1().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap(double lat, double lon) {

        if (googleMap==null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(lat, lon)).zoom(12).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // create marker
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title("shuttleQ map");

            // ROSE color icon change it to a shuttle icon TODO
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

            // adding marker
            googleMap.addMarker(marker);

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                Intent i = new Intent(getApplicationContext(), SendOrReceive.class);
                startActivity(i);
            }
        } else {

            googleMap.clear();
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(lat, lon)).zoom(14).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // create marker
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title("shuttleQ map");

            // ROSE color icon change it to a shuttle icon TODO
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

            // adding marker
            googleMap.addMarker(marker);
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                Intent i = new Intent(getApplicationContext(), SendOrReceive.class);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopThreadCount = 1;

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopThreadCount=1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(startThreadCount==0){
            startThreadCount=1;
        } else {
            getLoc2=new GetLocation2();
            getLoc2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class GetLocation1 extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Inside", "onPreExecute");
            pDialog = new ProgressDialog(ReceiveLocation.this);
            pDialog.setMessage("Loading location. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            Toast.makeText(getApplicationContext(),
                    url_get_location, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {
            int success;
            Log.d("Inside", "doInBackground");
            try {
                List<NameValuePair> param = new ArrayList<NameValuePair>();

                param.add(new BasicNameValuePair("route", routename));


                JSONObject json = jsonParser.makeHttpRequest(url_get_location, "GET", param);

                Log.d("Location details", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    latitude = json.getDouble("latitude");
                    longitude = json.getDouble("longitude");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initilizeMap(latitude, longitude);
                        }
                    });
                    return "pass";
                } else {
                    return "fail";
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            return "fail";
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String result) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if(result.equalsIgnoreCase("fail")) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Seems like nobody is currently sending the location", Toast.LENGTH_SHORT)
                        .show();
                Intent i=new Intent(getApplicationContext(),SendOrReceive.class);
                startActivity(i);
            } else {
                getLoc2=new GetLocation2();
                getLoc2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    }

    class GetLocation2 extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Inside", "onPreExecute");
        }

        @Override
        protected String doInBackground(String... params) {
            int success;
            Log.d("Inside", "doInBackground");
            try {
                while(true) {
                    if(stopThreadCount==1){
                        stopThreadCount=0;
                        return "true";
                    }
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    session = new SessionManager(getApplicationContext());
                    session.checkLogin();
                    HashMap<String, String> user = session.getUserDetails();
                    String routename = user.get(SessionManager.KEY_ROUTE);
                    param.add(new BasicNameValuePair("route", routename));

                    JSONObject json = jsonParser.makeHttpRequest(url_get_location, "GET", param);

                    Log.d("Location details", json.toString());

                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        latitude = json.getDouble("latitude");
                        longitude = json.getDouble("longitude");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initilizeMap(latitude, longitude);
                            }
                        });
                        Thread.sleep(10000);

                    } else {
                        return "fail";
                    }

                }
            }catch(JSONException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            return "fail";
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String result) {

            if(result.equalsIgnoreCase("fail")) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Seems like some error has occurred", Toast.LENGTH_SHORT)
                        .show();
                Intent i=new Intent(getApplicationContext(),SendOrReceive.class);
                startActivity(i);
            } else {
                // don't think code will ever reach this block

            }
        }

    }
}
