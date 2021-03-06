package cz.bosh.imageupload;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by honza on 4.5.17.
 */

public class ImageApplication extends Application {
    public static Application imageApplication;
    public static MainActivity mainActivity = null;
    public static ImageActivity imageActivity = null;
    public static boolean isPostRunning = false;
    public static String currentPhotoPath = null;
    public static String login;
    public static String password;

    private static List<String> csv = null;

    public static Database database;

    @Override
    public void onCreate() {
        imageApplication = this;
        super.onCreate();

        final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        login = prefs.getString(Settings.PREFS_LOGIN, null);
        password = prefs.getString(Settings.PREFS_PASSWORD, null);
        String csvText = prefs.getString(Settings.PREFS_CSV, null);
        if (csvText != null) {
            parseCsv(csvText);
        }
        database = new Database(this);
    }

    public static synchronized void parseCsv(String text) {
        String[] lines = text.split("\n");
        csv = new ArrayList<String>(lines.length);
        for (String line : lines) {
            csv.add(line);
        }

        Collections.sort(csv, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] fields = o1.split("\\$");
                String s1 = fields[0] + ',' + fields[2] + ',' + fields[1];
                fields = o2.split("\\$");
                String s2 = fields[0] + ',' + fields[2] + ',' + fields[1];

                return s1.compareTo(s2);
            }
        });
    }

    public static synchronized  List<String> getCsv() {
        // TODO copy?
        return csv;
    }
}
