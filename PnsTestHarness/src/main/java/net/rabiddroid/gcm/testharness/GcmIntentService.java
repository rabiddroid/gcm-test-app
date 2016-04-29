package net.rabiddroid.gcm.testharness;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.rabiddroid.gcm.testharness.model.ReceivedNotifications;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final String MSG_NOTIFICATION_RECEIVED = "Notification received.";
    public static final Uri NOTIFICATION_SOUND_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    String TAG = MainActivity.TAG;
    private SQLiteOpenHelper mDbHelper;
    private java.lang.String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        Log.d(TAG, "Received message type = " + messageType);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                                         extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Log.d(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                //Save the notification to database
                saveNotificationToDb(extras);
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.d(TAG, "Received: " + extras.toString());

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void saveNotificationToDb(Bundle extras) {

        mDbHelper = new PnsTestHarnessDbHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_SENDER, (String) extras.get("from"));

        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        String receivedDateTime = sdf.format(new Date());
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_DATE_TIME, receivedDateTime);


        //remove known artifacts
        extras.remove("from");
        extras.remove("android.support.content.wakelockid");
        values.put(ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_BODY, extras.toString());

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
}
