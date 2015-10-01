package com.qualcomm.ashish.shuttleq;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by ashish on 6/23/15.
 */
public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "shuttleQ_Pref";
    private static final String IS_LOGIN = "IsLoggenIn";
    public static final String KEY_USERNAME = "name";
    public static final String KEY_ROUTE = "1";
    public static final String KEY_IP = "192.168.0.101";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, int route, String Ip) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, name);
        editor.putInt(KEY_ROUTE, route);
        editor.putString(KEY_IP, Ip);
        editor.commit();
    }

    public void checkLogin(){
        if(!this.isLoggenIn()){
            Intent i=new Intent(_context,LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user=new HashMap<String, String>();
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_ROUTE, String.valueOf(pref.getInt(KEY_ROUTE, 0)));
        user.put(KEY_IP, pref.getString(KEY_IP, null));
        return user;
    }

    public void logOutUser(){
        editor.clear();
        editor.commit();
        Intent i=new Intent(_context,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public boolean isLoggenIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}


