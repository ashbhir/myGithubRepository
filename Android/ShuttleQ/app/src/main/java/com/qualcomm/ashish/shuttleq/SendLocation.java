package com.qualcomm.ashish.shuttleq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SendLocation extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    // LogCat tag
    private static final String TAG = SendLocation.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    protected GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 30 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 1; // 10 meters

    // UI elements
    private TextView locationText;
    private Button showLocationButton, toggleLocationButton;
    private double latitude;
    private double longitude;
    JSONParser jsonParser = new JSONParser();
    private String url_add_location = "http://192.168.0.101/android_connect/set_user_info.php";
    private String url_update_location = "http://192.168.0.101/android_connect/update_user_info.php";
    private final String TAG_SUCCESS = "success";
    private ProgressDialog pDialog;
    SessionManager session;
    private static int count=0;

    GoogleCloudMessaging gcmObj;
    static final String GOOGLE_PROJ_ID = "875703609912";
    String regId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_location);

        locationText = (TextView) findViewById(R.id.locationtext);
        showLocationButton = (Button) findViewById(R.id.showlocationbutton);
        toggleLocationButton = (Button) findViewById(R.id.togglelocationbutton);


        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }


        // Show location button click listener
        showLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // Toggling the periodic location updates
        toggleLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private HashMap<String,String> displayLocation() {
        HashMap<String, String> location=new HashMap<String, String>();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

           // locationText.setText(latitude + ", " + longitude);
            location.put("latitude", String.valueOf(latitude));
            location.put("longitude", String.valueOf(longitude));

        } else {

            locationText
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
            location.put("latitude", String.valueOf(0));
            location.put("longitude", String.valueOf(0));
        }
        return location;
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            toggleLocationButton
                    .setText("Sending location periodically");

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            toggleLocationButton
                    .setText("Stopped sending location periodically");

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    protected boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if(count<5) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Save the location to database TODO
        Log.i(TAG, "OnLocationChnaged");
        // Assign the new location
        mLastLocation = location;

        count++;

        Toast.makeText(getApplicationContext(), "Location changed! Count="+String.valueOf(count),
                Toast.LENGTH_SHORT).show();



        if (count==5) {
            count++;
            // Displaying the new location on UI
            new AddLocation().execute();
            stopLocationUpdates();
        }
    }

    class AddLocation extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "AddLocation Background");
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            HashMap<String, String> user= session.getUserDetails();
            String username=user.get(SessionManager.KEY_USERNAME);
            String routename=user.get(SessionManager.KEY_ROUTE);
            HashMap<String,String> location=displayLocation();
            String latitude=location.get("latitude");
            String longitude=location.get("longitude");

            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("username", username));
            param.add(new BasicNameValuePair("route", routename));
            param.add(new BasicNameValuePair("latitude", latitude));
            param.add(new BasicNameValuePair("longitude", longitude));

            try {
               if (gcmObj == null) {
                    gcmObj = GoogleCloudMessaging.getInstance(getApplicationContext());
                }

                regId = gcmObj.register(GOOGLE_PROJ_ID);

                param.add(new BasicNameValuePair("GCM_regid", regId));  // fix this TODO

                JSONObject json = jsonParser.makeHttpRequest(url_add_location,
                        "POST", param);
               // JSONObject json1 = jsonParser.makeHttpRequest(url_update_location,"POST", param);

                // check for success tag

                int success = json.getInt(TAG_SUCCESS);

                Looper.prepare();

                if (success == 1) {
                    // successfully created product
                    Toast.makeText(getApplicationContext(), "Successfully added!",
                            Toast.LENGTH_LONG).show();
                } else {
                    // failed to create product

                    Toast.makeText(getApplicationContext(), "Failed to add!",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException io) {
                io.printStackTrace();
            }

           // jsonParser.makeHttpRequest(url_update_location,
           //         "POST", param);

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String dummy) {


        }
    }

}