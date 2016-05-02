package net.rabiddroid.gcm.testharness;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by jeffreypthomas on 4/30/16.
 */
public class DeviceToken {

    /**
     * Stores the registration ID in the application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param deviceToken   registration ID
     */
    public void save(Context context, String deviceToken) {

        if (deviceToken == null || deviceToken.isEmpty()) {
            Log.w(LoggingPreferences.TAG, "Unable to store regid as it is null/empty");
            return;
        }


        final SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppPreferences.REGISTRATION_ID,deviceToken);
        editor.commit();

        Log.i(LoggingPreferences.TAG,"Saved deviceToken = "+deviceToken);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public String get(Context context){
        final SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(AppPreferences.REGISTRATION_ID,"");
    }


}
