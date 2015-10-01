package com.qualcomm.ashish.shuttleq;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by ashish on 7/16/15.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // check what this ComponentName is for TODO
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMNotificationService.class.getName());
        // check what is this startWakefulService TODO
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}