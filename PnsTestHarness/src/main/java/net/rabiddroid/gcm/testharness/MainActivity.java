package net.rabiddroid.gcm.testharness;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "1";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /**
     * Tag used on log messages.
     */
    public static final String TAG = "PnsTestHarness";
    static final String SENDER_ID = "110532548475";


    private Context context;
    private GoogleCloudMessaging gcm;
    private String regid;
    private TextView frontText;
    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frontText = (TextView) findViewById(R.id.textAlertView);

        context = getApplicationContext();

        //database setup
        mDbHelper = new PnsTestHarnessDbHelper(context);
        db = mDbHelper.getReadableDatabase();


        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                Log.d(TAG, "Registration ID=" + regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


        reloadNotificationsCountMessage();

        Log.d(TAG, "onCreate end");


        final Button buttonShowNotificationsReceived = (Button) findViewById(R.id.button_show_msgs);
        buttonShowNotificationsReceived.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //show list of messages on a new activity/screen
                Intent intent = new Intent(context, DisplayNotificationMessagesActivity.class);
                startActivity(intent);
            }
        });


        final Button buttonClearNotifications = (Button) findViewById(R.id.button_clear_msgs);
        buttonClearNotifications.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //show list of messages on a new activity/screen
                getContentResolver().delete(ReceivedNotificationsContentProvider.CONTENT_URI, null, null);

                reloadNotificationsCountMessage();
            }
        });

        final Button buttonSendAppToken = (Button) findViewById(R.id.button_send_app_token);
        buttonSendAppToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (regid.isEmpty()) {
                    Toast deviceTokenNotAvailableToast = Toast.makeText(getApplicationContext(),
                                                                        "The app token is not available yet. Try reloading the app.",
                                                                        Toast.LENGTH_LONG);
                    deviceTokenNotAvailableToast.show();
                } else {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("text/html");
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GCM TestPnsHarness app token");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(
                            String.format("<b>App token is %s</b>", regid)));
                    startActivity(Intent.createChooser(emailIntent, "Email to Friend"));
                }


            }
        });


    }

    private void reloadNotificationsCountMessage() {


        clearAnyExistingNotifications();


        int notificationsCount = ((PnsTestHarnessDbHelper) mDbHelper).getNotificationsCount();
        if (notificationsCount > 0) {
            frontText.setText("Number of notifications received = " + notificationsCount);
        } else {
            frontText.setText(R.string.welcome_message);
        }
    }

    private void clearAnyExistingNotifications() {

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.
                                         NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send messages to your app.
     * Not needed for this demo since the device sends upstream messages to a server that echoes back the message using
     * the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    Log.d(TAG, "SenderID=" + SENDER_ID);
                    regid = gcm.register(SENDER_ID);
                    Log.d(TAG, "Reg id=" + regid);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e(TAG, "Error", ex);

                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                Toast deviceRegisteredToast = Toast.makeText(getApplicationContext(),
                                                             "Device successfully registered for pns.",
                                                             Toast.LENGTH_SHORT);
                deviceRegisteredToast.show();
            }
        }.execute(null, null, null);
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                                             .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName() + "PnsTestHarness",
                                    Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows
     * users to download the APK from the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                                                      PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


}
