package net.rabiddroid.gcm.testharness;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

import net.rabiddroid.gcm.testharness.model.ReceivedNotifications;

/**
 * Created by jeffreypthomas on 11/24/14.
 */
public class DisplayNotificationMessagesActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private SimpleCursorAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifications_list);
        fillData();
        clearAnyExistingNotifications();

    }

    private void clearAnyExistingNotifications() {

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.
                                         NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }

    private void fillData() {

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from =
                new String[]{ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_DATE_TIME, ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_BODY};
        // Fields on the UI to which we map
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, from,
                                          to, 0);

        setListAdapter(adapter);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection =
                {ReceivedNotifications.ReceivedNotificationsEntry._ID, ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_DATE_TIME, ReceivedNotifications.ReceivedNotificationsEntry.COLUMN_NAME_BODY};
        CursorLoader cursorLoader = new CursorLoader(this,
                                                     ReceivedNotificationsContentProvider.CONTENT_URI, projection, null,
                                                     null,
                                                     ReceivedNotifications.ReceivedNotificationsEntry._ID + " DESC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
