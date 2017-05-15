package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private LocationManager mlocManager;
    private LocationListener mlocListener;
    private TextView mLongitude;
    private TextView mLatitude;
    private TextView mAccuracy;
    private View mUploadButton;
    private View mProgressBar;

    private static double lon = 0;
    private static double lat = 0;
    private static double acc = 0;

    protected void setThreadControls() {
        if (ImageApplication.isPostRunning) {
            mProgressBar.setVisibility(View.VISIBLE);
            mUploadButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mUploadButton.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImageApplication.mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLongitude = (TextView) findViewById(R.id.activity_main_longitude);
        mLatitude = (TextView) findViewById(R.id.activity_main_latitude);
        mAccuracy = (TextView) findViewById(R.id.activity_main_accuracy);
        mProgressBar = findViewById(R.id.activity_main_progress);

        mUploadButton = findViewById(R.id.activity_main_upload_button);

        setThreadControls();

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
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
    protected void onDestroy() {
        ImageApplication.mainActivity = null;
        super.onDestroy();
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

    protected void prepareAndStartPost() {
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("lon", String.valueOf(lon));
        map.put("lat", String.valueOf(lat));
        map.put("acc", String.valueOf(acc));

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = ImageApplication.imageApplication.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        String imageLocation = null;

        // Put it in the image view
        if (cursor.moveToFirst()) {

            imageLocation = cursor.getString(1);

        }
        cursor.close();

        ImageApplication.isPostRunning = true;
        setThreadControls();
        new PostThread("http://backpropagation.wz.cz/bosh/add.php", map, imageLocation).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 1)
            return;
        if (resultCode != RESULT_OK)
            return;
        Toast.makeText(ImageApplication.imageApplication, R.string.sending_gps , Toast.LENGTH_LONG).show();
        prepareAndStartPost();
     //   ;
    }

    protected void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        startActivityForResult(intent, 1);
    }

    public void onPostFinished() {
        ImageApplication.isPostRunning = false;
        setThreadControls();
    }
}
