package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Garry on 22/01/2018.
 */

public class StartupReceiver extends BroadcastReceiver {
    private static String TAG = StartupReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received Broadcast Intent" + intent.getAction());
        boolean isOn = QueryPreferences.getPrefIsAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
