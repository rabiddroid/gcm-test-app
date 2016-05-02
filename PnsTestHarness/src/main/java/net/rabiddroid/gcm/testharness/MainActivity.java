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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


public class MainActivity extends Activity {

    /**
     * Tag used on log messages.
     */
    static final String SENDER_ID = "110532548475";


    private Context context;
    private GoogleCloudMessaging gcm;
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


        // Check device for Play Services APK.//IF no device token exists
        if (new DeviceToken().get(getApplicationContext()).isEmpty()) {
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }


        reloadNotificationsCountMessage();


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

                String registrationId = new DeviceToken().get(getApplicationContext());

                if (registrationId.isEmpty()) {
                    Toast deviceTokenNotAvailableToast = Toast.makeText(getApplicationContext(),
                            "The app token is not available yet. Try reloading the app.",
                            Toast.LENGTH_LONG);
                    deviceTokenNotAvailableToast.show();
                } else {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("text/html");
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GCM TestPnsHarness app token");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(
                            String.format("<b>App token is %s</b>", registrationId)));
                    startActivity(Intent.createChooser(emailIntent, "Email to Friend"));
                }


            }
        });


        final Button buttonGetToken = (Button) findViewById(R.id.button_getToken);
        buttonGetToken.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(getApplicationContext(), RegistrationIntentService.class);
                    startService(intent);
                }
            }
        });

        final Button buttonRefreshToken = (Button) findViewById(R.id.button_RefreshToken);
        buttonRefreshToken.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(getApplicationContext(), RefreshTokenService.class);
                    startService(intent);
                }
            }
        });

        Log.d(LoggingPreferences.TAG, "onCreate end");
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
        if (new DeviceToken().get(getApplicationContext()).isEmpty()) {
            checkPlayServices();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows
     * users to download the APK from the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        Log.d(LoggingPreferences.TAG, "Checking for play services");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        AppPreferences.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LoggingPreferences.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


}
