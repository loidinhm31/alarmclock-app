package com.example.alarmclock.database;

import android.provider.BaseColumns;

public final class Schema {
    private Schema() {}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "alarm_table";
        public static final String ALARM_HOUR = "alarm_hour";
        public static final String ALARM_MINUTE = "alarm_minute";
        public static final String ALARM_ENABLED = "alarm_enable";
    }

}
