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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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

    private static double lon = 0;
    private static double lat = 0;
    private static double acc = 0;

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
                lon = location.getLongitude();
                lat = location.getLatitude();
                acc = location.getAccuracy();

                mLongitude.setText(String.valueOf(lon));
                mLatitude.setText(String.valueOf(lat));
                mAccuracy.setText(String.valueOf(acc));


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

    private static void doPostWithImage(String url, Map<String, String> args, String pathToImage) {

        try {

            File binaryFile = new File(pathToImage);
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            final String charset = "UTF-8";

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);


            // Send normal params
            for (Map.Entry<String, String> entry : args.entrySet()) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).append(entry.getValue()).append(CRLF).flush();
            }

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();


            InputStream input = new FileInputStream(binaryFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, bytesRead);
            }
            input.close();


            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();

            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            Toast.makeText(ImageApplication.imageApplication, "Server response: " + String.valueOf(responseCode), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ImageApplication.imageApplication, e.toString(), Toast.LENGTH_LONG).show();Toast.makeText(ImageApplication.imageApplication, e.toString(), Toast.LENGTH_LONG).show();
        }

    }


    private static void doPost(String url, Map<String, String> args) {

        try {
            Toast.makeText(ImageApplication.imageApplication, "Sending GPS coordinates..." , Toast.LENGTH_LONG).show();
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

            Toast.makeText(ImageApplication.imageApplication, "Server response: " + String.valueOf(responseCode), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(ImageApplication.imageApplication, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    protected void postData() {
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("lon", String.valueOf(lon));
        map.put("lat", String.valueOf(lat));
        map.put("acc", String.valueOf(acc));
      //  doPost("http://backpropagation.wz.cz/bosh/add.php", map);

        
        doPostWithImage("http://backpropagation.wz.cz/bosh/add.php", map, "/storage/sdcard1/DCIM/Camera/i.jpg");
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
