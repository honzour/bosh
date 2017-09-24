package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private View mLogin;
    private View mPhoto;


    protected void login() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImageApplication.mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mLogin = findViewById(R.id.main_login);
        mPhoto = findViewById(R.id.main_photo);


        if (ImageApplication.login == null) {
            mPhoto.setEnabled(false);
            login();
        }


    }

    @Override
    protected void onDestroy() {
        ImageApplication.mainActivity = null;
        super.onDestroy();
    }

}
