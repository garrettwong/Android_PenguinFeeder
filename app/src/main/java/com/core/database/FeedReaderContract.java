package com.core.database;

import android.provider.BaseColumns;

/**
 * Created by GWM on 2/5/17.
 * https://developer.android.com/training/basics/data-storage/databases.html
 */
public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "scores";

        public static final String COLUMN_NAME_USERID = "userid";
        public static final String COLUMN_NAME_SCORE = "score";
    }
}