package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Garry on 22/01/2018.
 */

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mSOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mSOnShowNotification);
    }

    private BroadcastReceiver mSOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "If we receive this we're visible so cancel the notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
