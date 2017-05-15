package cz.bosh.imageupload;

import android.app.Application;

/**
 * Created by honza on 4.5.17.
 */

public class ImageApplication extends Application {
    public static Application imageApplication;
    public static MainActivity mainActivity = null;
    public static boolean isPostRunning = false;

    @Override
    public void onCreate() {
        imageApplication = this;
        super.onCreate();
    }
}
