package cz.bosh.imageupload;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by honza on 25.10.17.
 */

public class Database extends SQLiteOpenHelper {

    public static class ShortRecord implements Serializable {
        public long id;
        public long shop;

        public ShortRecord(long id, long shop) {
            this.id = id;
            this.shop = shop;
        }
    }

    public static class Record implements Serializable {
        public String filename;
        public byte[] image;
        public Map<String, String> map;

        public Record(String filename, byte[] image, Map<String, String> map) {
            this.filename = filename;
            this.image = image;
            this.map = map;
        }
    }

    private static final String TABLE_NAME_POSTS = "POSTS";
    private static final String TABLE_NAME_ARGS = "ARGS";

    private static final String COLUMN_NAME_ID = "ID";
    private static final String COLUMN_NAME_FILENAME = "FILENAME";
    private static final String COLUMN_NAME_IMAGE = "IMAGE";
    private static final String COLUMN_NAME_ID_POST = "ID_POST";
    private static final String COLUMN_NAME_NAME = "NAME";
    private static final String COLUMN_NAME_VALUE = "VALUE";

    private static final String SQL_CREATE_POSTS = "CREATE TABLE " + TABLE_NAME_POSTS + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME_FILENAME + " VARCHAR, "  + COLUMN_NAME_IMAGE + " BLOB)";
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

    public long insert(Record record) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_IMAGE, record.image);
        values.put(COLUMN_NAME_FILENAME, record.filename);
        long newRowId = db.insert(TABLE_NAME_POSTS, null, values);


        for (Map.Entry<String, String> e : record.map.entrySet()) {
            values = new ContentValues();
            values.put(COLUMN_NAME_ID_POST, newRowId);
            values.put(COLUMN_NAME_NAME, e.getKey());
            values.put(COLUMN_NAME_VALUE, e.getValue());
            db.insert(TABLE_NAME_ARGS, null, values);
        }

        return newRowId;
    }

    public void delete(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME_ARGS, COLUMN_NAME_ID_POST + " = ?", whereArgs);
        db.delete(TABLE_NAME_POSTS, COLUMN_NAME_ID + " = ?", whereArgs);
    }

    public List<ShortRecord> selectAll() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_ID
        };

        String sortOrder = COLUMN_NAME_ID + " DESC";

        /*Cursor cursor = db.query(
                TABLE_NAME_POSTS,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        */

        String sql = "SELECT p." + COLUMN_NAME_ID + ", a." + COLUMN_NAME_VALUE +
                " FROM " + TABLE_NAME_POSTS + " p LEFT JOIN " + TABLE_NAME_ARGS + " a ON p." +
                COLUMN_NAME_ID + " = a." + COLUMN_NAME_ID_POST + " WHERE a." + COLUMN_NAME_NAME +
                " = 'shop' OR a." + COLUMN_NAME_NAME + " IS NULL";
        Cursor cursor = db.rawQuery(sql, null);

        List<ShortRecord> itemIds = new ArrayList<ShortRecord>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_ID));
            long shop = cursor.getLong(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_VALUE));
           itemIds.add(new ShortRecord(itemId, shop));
        }
        cursor.close();
        return itemIds;
    }

    public Record selectById(long id) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_FILENAME,
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
        String filename = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_FILENAME));
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

        return new Record(filename, b, map);
    }
}
