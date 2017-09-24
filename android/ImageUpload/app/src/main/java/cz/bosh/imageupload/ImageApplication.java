package cz.bosh.imageupload;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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

    @Override
    public void onCreate() {
        imageApplication = this;
        super.onCreate();

        final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        login = prefs.getString(Settings.PREFS_LOGIN, null);
        password = prefs.getString(Settings.PREFS_PASSWORD, null);
    }
}
