package net.rabiddroid.gcm.testharness;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.rabiddroid.gcm.testharness.model.ReceivedNotifications;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PnsGcmListenerService extends GcmListenerService {
    public static final int NOTIFICATION_ID = 1;
    public static final String MSG_NOTIFICATION_RECEIVED = "Notification received.";
    public static final Uri NOTIFICATION_SOUND_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private SQLiteOpenHelper mDbHelper;
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        Log.d(LoggingPreferences.TAG, "Received notification message signal");
        if (!data.isEmpty()) {  // has effect of unparcelling Bundle
            final String message = data.toString();
            Log.d(LoggingPreferences.TAG, "Completed work @ " + SystemClock.elapsedRealtime());
            //Save the notification to database
            saveNotificationToDb(from, message);
            // Post notification of received message.
            Log.d(LoggingPreferences.TAG, "Received: " + message);
            sendNotification("Received: " + message);
        }

    }

    private void saveNotificationToDb(String from, String message) {

        mDbHelper = new PnsTestHarnessDbHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_SENDER, from);

        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        String receivedDateTime = sdf.format(new Date());
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_DATE_TIME, receivedDateTime);


        //remove known artifacts;
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_BODY, message);

        //save data to database via content provider
        getContentResolver().insert(ReceivedNotificationsContentProvider.CONTENT_URI, values);


    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {


        /**
         * Building the targetted intent with parent in backstack
         */
        Intent targetIntent = new Intent(this, DisplayNotificationMessagesActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(targetIntent);
        PendingIntent notificationIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        /**
         * Build the notification to be sent
         */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_alert)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(MSG_NOTIFICATION_RECEIVED).setOnlyAlertOnce(true).setSound(
                        NOTIFICATION_SOUND_URI).setContentIntent(notificationIntent);

        /**
         * Send the notification
         */
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    /*private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }*/
}
