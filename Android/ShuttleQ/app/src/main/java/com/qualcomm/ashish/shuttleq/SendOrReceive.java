package com.qualcomm.ashish.shuttleq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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

/**
 * Created by ashish on 6/21/15.
 */
public class SendOrReceive extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // the UI elements
    private Button sendButton,receiveButton, deleteButton;
    private TextView usernameText, RouteText;

    // session manager
    private SessionManager session;
    private String username;
    private String routename;
    private String Ipaddr;

    // JSON parser
    private JSONParser jsonParser=new JSONParser();

    // TAGS for parsing JSON inputs
    public final String TAG_SUCCESS="success";
    public final String TAG_MESSAGE="message";

    // Server files
    public String url_add_location;
    public String url_delete_location;
    public String url_update_location;

    // Location variables
    private Location mLastLocation;
    private double latitude;
    private double longitude;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    protected GoogleApiClient mGoogleApiClient;

    // Google GCM variables
    private GoogleCloudMessaging gcmObj;
    static final String GOOGLE_PROJ_ID = "875703609912";
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_receive);

        // Initialise the UI elements
        sendButton=(Button)findViewById(R.id.sendbutton);
        receiveButton=(Button)findViewById(R.id.receivebutton);
        deleteButton=(Button)findViewById(R.id.deletebutton);
        usernameText=(TextView)findViewById(R.id.textUsername);
        RouteText=(TextView)findViewById(R.id.textRoute);

        // Initialise the Session manager
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user= session.getUserDetails();
        username=user.get(SessionManager.KEY_USERNAME);
        routename=user.get(SessionManager.KEY_ROUTE);
        Ipaddr=user.get(SessionManager.KEY_IP);

        url_add_location = "http://"+Ipaddr+"/android_connect/set_user_info.php";
        url_delete_location = "http://"+Ipaddr+"/android_connect/delete_user_info.php";
        url_update_location = "http://"+Ipaddr+"/android_connect/update_user_info.php";

        // Set the intro text
        usernameText.setText(Html.fromHtml("Hi! Welcome <b>"+username));
        RouteText.setText(Html.fromHtml("You are subscribed to <b>Route "+routename));

        // Location initialiser
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        // set all the on click listeners

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddLocation addLoc=new AddLocation();
                addLoc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),ReceiveLocation.class);
                startActivity(i);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteLocations delLoc=new DeleteLocations();
                delLoc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    private HashMap<String,String> displayLocation() {
        HashMap<String, String> location=new HashMap<String, String>();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            location.put("latitude", String.valueOf(latitude));
            location.put("longitude", String.valueOf(longitude));

        } else {
            location.put("latitude", null);
            location.put("longitude", null);
        }
        return location;
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

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Error", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    class DeleteLocations extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),
                    url_delete_location, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {
            int success;
            try {
                    List<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("route", routename));

                    JSONObject json = jsonParser.makeHttpRequest(url_delete_location, "GET", param);
                    Log.d("Delete details", json.toString());
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        return "pass";

                    } else {
                        return "fail";
                    }

            }catch(JSONException e){
                e.printStackTrace();
            }
            return "fail";
        }

        protected void onPostExecute(String result) {

            if(result.equalsIgnoreCase("fail")) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Seems like some error has occurred, please retry", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Thank you for using the app! Have a nice day", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    class AddLocation extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),
                    url_add_location, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String,String> location=displayLocation();

            String latitude=location.get("latitude");
            String longitude=location.get("longitude");

            List<NameValuePair> param = new ArrayList<NameValuePair>();
            if ((latitude!=null) && (longitude!=null)) {
                param.add(new BasicNameValuePair("latitude", latitude));
                param.add(new BasicNameValuePair("longitude", longitude));
                param.add(new BasicNameValuePair("username", username));
                param.add(new BasicNameValuePair("route", routename));
            } else {
                return "fail";
            }

            try {
                if (gcmObj == null) {
                    gcmObj = GoogleCloudMessaging.getInstance(getApplicationContext());
                }

                regId = gcmObj.register(GOOGLE_PROJ_ID);

                if (regId != null) {
                    param.add(new BasicNameValuePair("GCM_regid", regId));
                }
                else {
                    return "fail";
                }

                JSONObject json = jsonParser.makeHttpRequest(url_add_location, "POST", param);

                // check for success tag
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    final String msg=json.getString(TAG_MESSAGE);

                    // check if this the first user
                    // if it is start a thread to periodically update the location
                    if(msg.contains("First")) {

                        // update the toast right now as the thread may take a very long time to complete
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "First User of this route: Your location is now pooled in", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                        // this executes a thread which will not end until all resources are deleted
                        JSONObject json1 = jsonParser.makeHttpRequest(url_update_location,"POST", param);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Your location is now pooled in", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }
                    return "pass";
                } else {
                    return "fail";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException io) {
                io.printStackTrace();
            }

            return "fail";
        }

        protected void onPostExecute(String result) {
            if(result.equalsIgnoreCase("fail")) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Seems like some error has occurred, please retry", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // do nothing
            }

        }
    }

}
