package net.rabiddroid.gcm.testharness.model;

import android.provider.BaseColumns;

/**
 * Created by jeffreypthomas on 11/24/14.
 */
public final class ReceivedNotifications {


    public ReceivedNotifications() {
    }


    public abstract class ReceivedNotificationsEntry implements BaseColumns{


        public static final String TABLE_NAME = "received_notifications";
        public static final String COLUMN_NAME_SENDER = "sender";
        public static final String COLUMN_NAME_BODY = "body";
        public static final String COLUMN_NAME_DATE_TIME = "date_time";



    }

}
