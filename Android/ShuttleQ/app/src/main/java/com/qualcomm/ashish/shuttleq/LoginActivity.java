package com.qualcomm.ashish.shuttleq;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by ashish on 6/23/15.
 */
public class LoginActivity extends Activity{


    // Username, server Ip
    EditText txtUsername;
    EditText serverIp;

    final Context context= this;

    Spinner routeNumber;

    // login button
    Button buttonLogin;

    // Session Manager Class
    SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        txtUsername = (EditText) findViewById(R.id.usernameVal);
        serverIp= (EditText) findViewById(R.id.server_address);
        routeNumber = (Spinner) findViewById(R.id.routeVal);
        buttonLogin = (Button) findViewById(R.id.loginbutton);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = txtUsername.getText().toString();
                String ipAddress = serverIp.getText().toString();
                Integer routeNum = Integer.parseInt(String.valueOf(routeNumber.getSelectedItem()));
                // Check if username, password is filled
                if (username.trim().length() > 0) {
                    // Creating user login session

                    session.createLoginSession(username, routeNum, ipAddress);

                    // Staring MainActivity
                    Intent i = new Intent(getApplicationContext(), SendOrReceive.class);
                    startActivity(i);
                    finish();
                } else {
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle("Error");

                    // set dialog message
                    alertDialogBuilder.setMessage("Enter some username!");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });


                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
        });

    }
}
