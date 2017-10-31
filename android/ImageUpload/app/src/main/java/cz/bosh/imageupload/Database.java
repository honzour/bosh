package cz.bosh.imageupload;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by honza on 25.10.17.
 */

public class Database extends SQLiteOpenHelper {

    public static class Record {
        public byte[] image;
        public Map<String, String> map;

        public Record(byte[] image, Map<String, String> map) {
            this.image = image;
            this.map = map;
        }
    }

    private static final String TABLE_NAME_POSTS = "POSTS";
    private static final String TABLE_NAME_ARGS = "ARGS";

    private static final String COLUMN_NAME_ID = "ID";
    private static final String COLUMN_NAME_IMAGE = "IMAGE";
    private static final String COLUMN_NAME_ID_POST = "ID_POST";
    private static final String COLUMN_NAME_NAME = "NAME";
    private static final String COLUMN_NAME_VALUE = "VALUE";

    private static final String SQL_CREATE_POSTS = "CREATE TABLE " + TABLE_NAME_POSTS + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME_IMAGE + " BLOB)";
    private static final String SQL_DROP_POSTS = "DROP TABLE IF EXISTS " + TABLE_NAME_POSTS;

    private static final String SQL_CREATE_ARGS = "CREATE TABLE " + TABLE_NAME_ARGS + " (" +
            COLUMN_NAME_ID_POST + " INT, " + COLUMN_NAME_NAME + " VARCHAR, " + COLUMN_NAME_VALUE +
            " VARCHAR)";
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

    public long insert(byte[] image, Map<String, String> args) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_IMAGE, image);
        long newRowId = db.insert(TABLE_NAME_POSTS, null, values);

        values = new ContentValues();
        values.put(COLUMN_NAME_ID_POST, newRowId);
        for (Map.Entry<String, String> e : args.entrySet()) {
            values.put(COLUMN_NAME_NAME, e.getKey());
            values.put(COLUMN_NAME_VALUE, e.getValue());
        }

        return newRowId;
    }

    public List<Long> selectAll() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_ID
        };

        String sortOrder = COLUMN_NAME_ID + " DESC";

        Cursor cursor = db.query(
                TABLE_NAME_POSTS,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        List<Long> itemIds = new ArrayList<Long>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_ID));
            itemIds.add(itemId);
        }
        cursor.close();
        return itemIds;
    }

    public Record selectById(long id) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_IMAGE
        };

        String[] selectionArgs = {
                String.valueOf(id)
        };

        Cursor cursor = db.query(
                TABLE_NAME_POSTS,                     // The table to query
                projection,                               // The columns to return
                COLUMN_NAME_ID + " = ?",                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        List<Long> itemIds = new ArrayList<Long>();
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        byte[] b = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMAGE));
        cursor.close();

        String[] projection2 = {
                COLUMN_NAME_NAME,
                COLUMN_NAME_VALUE
        };

        cursor = db.query(
                TABLE_NAME_ARGS,                     // The table to query
                projection2,                               // The columns to return
                COLUMN_NAME_ID_POST + " = ?",                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        Map<String, String> map = new HashMap<String, String>();

        while(cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME));
            String value = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_VALUE));
            map.put(name, value);
        }

        return new Record(b, map);
    }
}
