package com.example.birdclassificationappfinal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BirdsDataSource {
    private static final String TAG = "BirdsDataSource";
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper sqLiteOpenHelper;
    private String DataBaseOpenedMessage = "Database opened.";
    private String DataBaseClosedMessage = "Database closed.";
    private String birdsFeedbackBirdName ="birds_name";
    private String birdsFeedbackUserFeedback ="user_feedback";
    private String birdsFeedbackTable = "birds_feedback";


    public BirdsDataSource(Context context) {
        sqLiteOpenHelper = new BirdsOpenDbHelper(context);
    }

    public void open() {
        sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
        Log.d(TAG, DataBaseOpenedMessage);
    }

    public void close() {
        sqLiteOpenHelper.close();
        Log.d(TAG, DataBaseClosedMessage);
    }

    //--------------------------InsertQuery-----------------------------
    public void insertDataBirdsFeedback(String bird_name, String user_feedback) {
        ContentValues newValues = new ContentValues();
        newValues.put(birdsFeedbackBirdName, bird_name);
        newValues.put(birdsFeedbackUserFeedback, user_feedback);
        sqLiteDatabase.insertOrThrow(birdsFeedbackTable, null, newValues);
    }

}
