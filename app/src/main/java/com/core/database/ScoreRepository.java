package com.core.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.core.score.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GWM on 6/5/17.
 */

public class ScoreRepository implements IRepository<Score> {
    private Context mContext;
    private FeedReaderDbHelper mDbHelper;

    public ScoreRepository(Context context) {
        mContext = context;
        mDbHelper = new FeedReaderDbHelper(this.mContext);
    }

    public void writeDatabaseData(int userId, double score) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_USERID, userId);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE, score);

        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    public void readDatabaseData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_USERID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_USERID + " = ?";
        String[] selectionArgs = { "My Title" };

// How you want the results sorted in the resulting Cursor
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);
        }

        cursor.close();
    }

    @Override
    public List<Score> getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_USERID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null, //selection,                                // The columns for the WHERE clause
                null, //selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);
        }

        cursor.close();

        return itemIds;
    }

    @Override
    public Score getById(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_USERID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE
        };

        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_USERID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        Score s = null;
        while(cursor.moveToNext()) {
            s = new Score(id, cursor.getDouble(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE)));
        }
        cursor.close();

        return s;
    }

    @Override
    public void add(Score score) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_USERID, score.getUserId());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SCORE, score.getValue());
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    @Override
    public void update(Score score) {

    }

    @Override
    public void delete(int id) {

    }
}
