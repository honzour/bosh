package cz.bosh.imageupload;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
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
    }

    public static synchronized void parseCsv(String text) {
        String[] lines = text.split("\n");
        csv = new ArrayList<String>(lines.length);
        for (String line : lines) {
            csv.add(line);
        }
    }

    public static synchronized  List<String> getCsv() {
        // TODO copy?
        return csv;
    }
}
