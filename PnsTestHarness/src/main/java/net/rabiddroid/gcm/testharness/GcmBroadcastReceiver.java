package net.rabiddroid.gcm.testharness;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by jeffreypthomas on 11/20/14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

private static final String TAG = MainActivity.TAG;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Received An Intent via broadcast");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                                               GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
