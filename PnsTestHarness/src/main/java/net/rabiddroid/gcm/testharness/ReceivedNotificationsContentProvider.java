package net.rabiddroid.gcm.testharness;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import net.rabiddroid.gcm.testharness.model.ReceivedNotifications;

public class ReceivedNotificationsContentProvider extends ContentProvider {

    private PnsTestHarnessDbHelper database;
    private static final String AUTHORITY = "net.rabiddroid.gcm.testharness.receivednotifications";

    private static final String BASE_PATH = ReceivedNotifications.ReceivedNotificationsEntry.TABLE_NAME;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                                                            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + ReceivedNotifications.ReceivedNotificationsEntry.TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/received_notification";

    public ReceivedNotificationsContentProvider() {
    }

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int RECEIVED_NOTIFICATION = 10;
    private static final int RECEIVED_NOTIFICATION_ID = 20;

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, RECEIVED_NOTIFICATION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RECEIVED_NOTIFICATION_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase db = database.getWritableDatabase();
        String whereClause = null;
        String whereClauseArgs = null;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case RECEIVED_NOTIFICATION:
                break;
            case RECEIVED_NOTIFICATION_ID:
                whereClause = ReceivedNotifications.ReceivedNotificationsEntry._ID + "= ?";
                whereClauseArgs = uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return db.delete(ReceivedNotifications.ReceivedNotificationsEntry.TABLE_NAME, selection, selectionArgs);

    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Insert the new row, returning the primary key value of the new row

        SQLiteDatabase db = database.getWritableDatabase();
        Long newRowId = db.insert(
                ReceivedNotifications.ReceivedNotificationsEntry.TABLE_NAME,
                null,
                values);

        //build the
        StringBuilder uriString = new StringBuilder();
        uriString.append("content://");
        uriString.append(AUTHORITY);
        uriString.append(BASE_PATH);
        uriString.append("/");
        uriString.append(newRowId);

        return Uri.parse(uriString.toString());
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        database = new PnsTestHarnessDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {


        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set the table
        queryBuilder.setTables(ReceivedNotifications.ReceivedNotificationsEntry.TABLE_NAME);

/*        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TODOS:
                break;
            case TODO_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(TodoTable.COLUMN_ID + "="
                                                 + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }*/

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                                           selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
