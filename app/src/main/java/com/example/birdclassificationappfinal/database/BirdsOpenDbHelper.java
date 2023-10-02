package com.example.birdclassificationappfinal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class BirdsOpenDbHelper extends SQLiteOpenHelper implements BaseColumns {
    private static final String DATABASE_NAME = "birds";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "BirdsOpenDbHelper";

    public BirdsOpenDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //create user_details table
    private static final String DATABASE_BIRDS_FEEDBACK_TABLE_CREATE = "create table " + "birds_feedback" + "( "
            + _ID + " integer primary key autoincrement,"
            + "birds_name  text,user_feedback text); ";
    //create pedometer table


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_BIRDS_FEEDBACK_TABLE_CREATE);
        Log.d(TAG, DATABASE_BIRDS_FEEDBACK_TABLE_CREATE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS 'birds_feedback'; ");
        Log.d(TAG, "DROP TABLE IF EXISTS 'birds_feedback'; ");
        onCreate(db);
    }
}
