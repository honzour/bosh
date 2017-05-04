package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {

    private LocationManager mlocManager;
    private LocationListener mlocListener;
    private TextView mLongitude;
    private TextView mLatitude;
    private TextView mAccuracy;
    private View mUploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLongitude = (TextView) findViewById(R.id.activity_main_longitude);
        mLatitude = (TextView) findViewById(R.id.activity_main_latitude);
        mAccuracy = (TextView) findViewById(R.id.activity_main_accuracy);
        mUploadButton = findViewById(R.id.activity_main_upload_button);

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startCamera();
                postData();
            }
        });

        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                mLongitude.setText(String.valueOf(location.getLongitude()));
                mLatitude.setText(String.valueOf(location.getLatitude()));
                mAccuracy.setText(String.valueOf(location.getAccuracy()));



            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, R.string.no_gps, Toast.LENGTH_LONG).show();
                mlocManager.removeUpdates(mlocListener);
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

        };
    }

    @Override
    protected void onPause() {
        if (mlocListener != null && mlocManager != null)
            mlocManager.removeUpdates(mlocListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(MainActivity.this, R.string.no_gps, Toast.LENGTH_LONG).show();
        }
        else
        {
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        }
    }

    private static void doPost(String url, Map<String, String> args) {

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Muj Browser");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            StringBuffer urlParameters = new StringBuffer();
            for (Map.Entry<String, String> entry : args.entrySet()) {

                if (urlParameters.length() > 0) {
                    urlParameters.append('&');
                }

                urlParameters.append(entry.getKey());
                urlParameters.append('=');
                urlParameters.append(entry.getValue());
            }

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Toast.makeText(ImageApplication.imageApplication, String.valueOf(responseCode), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    protected void postData() {
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("lon", "14");
        map.put("lat", "50");
        map.put("acc", "7");
        doPost("http://backpropagation.wz.cz/bosh/add.php", map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 1)
            return;
        if (resultCode != RESULT_OK)
            return;
        postData();
    }

    protected void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        startActivityForResult(intent, 1);
    }
}
