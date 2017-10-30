package cz.bosh.imageupload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private Button mLogin;
    private View mPhoto;
    private View mSaved;

    private static final int DIALOG_LOGIN = 1;

    protected void login() {
        showDialog(DIALOG_LOGIN);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.login, null);
        final TextView login = (TextView) view.findViewById(R.id.login_login);
        final TextView password = (TextView) view.findViewById(R.id.login_password);

        login.setText(ImageApplication.login);
        password.setText(ImageApplication.password);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        return builder.setTitle(R.string.login).
                setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final String loginText = login.getText().toString();
                        final String passwordText = password.getText().toString();

                        final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(Settings.PREFS_LOGIN, loginText);
                        edit.putString(Settings.PREFS_PASSWORD, passwordText);
                        edit.commit();
                        ImageApplication.login = loginText;
                        ImageApplication.password = passwordText;

                        Map<String, String> map = new HashMap<String, String>();
                        map.put("login", loginText);
                        map.put("password", passwordText);

                        new CsvDownloadPostThread(Settings.URL_BASE + Settings.URL_END_CSV, map).start();
                    }
                }).
                setNegativeButton(R.string.cancel, null).
                setView(view).create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImageApplication.mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mLogin = (Button)findViewById(R.id.main_login);
        mPhoto = findViewById(R.id.main_photo);
        mSaved = findViewById(R.id.main_saved);

        if (ImageApplication.login == null) {
            mPhoto.setEnabled(false);
            mSaved.setEnabled(false);
            login();
        }

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ImageActivity.class));
            }
        });
        mSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Long> ids = ImageApplication.database.selectAll();
                for (Long l : ids) {
                    Log.i("IDJE", l.toString());
                }
            }
        });
    }

    protected void setButtons() {
        boolean isCsv = (ImageApplication.getCsv() != null);
        mPhoto.setEnabled(isCsv);
        mSaved.setEnabled(isCsv);
        mLogin.setText(isCsv ? R.string.relogin : R.string.login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageApplication.currentPhotoPath = null;
        setButtons();
    }

    @Override
    protected void onDestroy() {
        ImageApplication.mainActivity = null;
        super.onDestroy();
    }

    public void onPostFinished() {
        setButtons();
    }
}
