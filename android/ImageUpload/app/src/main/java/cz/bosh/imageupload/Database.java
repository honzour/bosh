package cz.bosh.imageupload;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by honza on 25.10.17.
 */

public class Database extends SQLiteOpenHelper {

    private static final String TABLE_NAME_POSTS = "POSTS";
    private static final String TABLE_NAME_ARGS = "ARGS";

    private static final String SQL_CREATE_POSTS = "CREATE TABLE " + TABLE_NAME_POSTS + " (ID INT, IMAGE BLOB)";
    private static final String SQL_DROP_POSTS = "DROP TABLE IF EXISTS " + TABLE_NAME_POSTS;

    private static final String SQL_CREATE_ARGS = "CREATE TABLE " + TABLE_NAME_ARGS + " (ID_POST INT, NAME VARCHAR, VALUE VARCHAR)";
    private static final String SQL_DROP_ARGS = "DROP TABLE IF EXISTS " + TABLE_NAME_ARGS;

    private static final String DATABASE_NAME = "bosch";

    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_POSTS);
        db.execSQL(SQL_CREATE_ARGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_ARGS);
        db.execSQL(SQL_DROP_POSTS);
        onCreate(db);
    }
}
