package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
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

public class ImageActivity extends Activity {

    class SelectItem {
        public int index;
        public double lon;
        public double lat;

        public SelectItem(int index, double lon, double lat) {
            this.index = index;
            this.lon = lon;
            this.lat = lat;
        }
    }

    private LocationManager mlocManager;
    private LocationListener mlocListener;
    private TextView mAccuracy;
    private TextView mNote;
    private TextView mNote2;
    private View mUploadButton;
    private View mSaveButton;
    private View mProgressBar;
    private Spinner mShop;
    private ImageView mImage;
    private CheckBox mUseGps;
    private CheckBox mTourplan;
    private CheckBox mOrder;

    private List<SelectItem> mShopData;

    private static double lon = 0;
    private static double lat = 0;
    private static double acc = 0;

    protected static final int RC_TAKE_PHOTO = 1;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    protected void setThreadControls() {
        if (ImageApplication.isPostRunning) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
         }
        updateButtons();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImageApplication.imageActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);

        mUploadButton = findViewById(R.id.image_upload_button);
        mSaveButton = findViewById(R.id.image_save_button);

        mAccuracy = (TextView) findViewById(R.id.image_accuracy);
        mNote = (TextView) findViewById(R.id.image_note);
        mNote2 = (TextView) findViewById(R.id.image_note2);
        mProgressBar = findViewById(R.id.image_progress);
        mShop = (Spinner) findViewById(R.id.image_shop);
        mImage = (ImageView) findViewById(R.id.image_image);
        mUseGps = (CheckBox) findViewById(R.id.image_use_gps);
        mTourplan = (CheckBox) findViewById(R.id.image_tourplan);
        mOrder = (CheckBox) findViewById(R.id.image_order);
        updateImage();

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });

        List<String> csv = ImageApplication.getCsv();

        List<String> list = new ArrayList<String>(csv.size() + 1);
        mShopData = new ArrayList<SelectItem>(list.size() + 1);

        mShopData.add(null);
        list.add("----------------------------");

        for (String line : csv) {
            SelectItem si = null;
            try {
                String[] fields = line.split("\\$");
                line = fields[0] + ',' + fields[1] + ',' + fields[2];
                si = new SelectItem(Integer.valueOf(fields[3]), Double.valueOf(fields[4]), Double.valueOf(fields[5]));
            } catch (Exception e) {
                continue;
                // TODO
            }

            mShopData.add(si);
            list.add(line);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.select_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mShop.setAdapter(adapter);




        setThreadControls();

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImageApplication.imageApplication, R.string.sending_gps , Toast.LENGTH_LONG).show();
                prepareAndStartPost();
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

                String descLon = String.valueOf(lon);
                if (descLon.length() > 5) descLon = descLon.substring(0, 5);

                String descLat = String.valueOf(lat);
                if (descLat.length() > 5) descLat = descLat.substring(0, 5);

                String descAcc = String.valueOf(acc);
                if (descAcc.length() > 5) descAcc = descAcc.substring(0, 5);

                mAccuracy.setText(descLon + "°/" + descLat + "°/" + descAcc + 'm');

                if (mUseGps.isChecked()) {

                    double min = -1;
                    int index = -1;
                    for (int i = 1; i < mShopData.size(); i++) {
                        SelectItem si = mShopData.get(i);
                        double dist = (si.lat - lat) * (si.lat - lat) + (si.lon - lon) * (si.lon - lon);
                        if (min < 0 || dist < min) {
                            min = dist;
                            index = i;
                        }
                    }
                    if (index > 0) {
                        mShop.setSelection(index);
                    }
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(ImageActivity.this, R.string.no_gps, Toast.LENGTH_LONG).show();
                mlocManager.removeUpdates(mlocListener);
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

        };

        mShop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateButtons();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updateButtons();
            }
        });
    }

    @Override
    protected void onDestroy() {
        ImageApplication.imageActivity = null;
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
            Toast.makeText(ImageActivity.this, R.string.no_gps, Toast.LENGTH_LONG).show();
        }
        else
        {
            // TODO min time
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        }
    }

    protected void prepareAndStartPost() {
        Map<String, String> map = new HashMap<String, String>(5);

        map.put("login", ImageApplication.login);
        map.put("password", ImageApplication.password);

        int pos = mShop.getSelectedItemPosition();

        SelectItem si = mShopData.get(pos);

        map.put("shop", String.valueOf(si == null ? -1 : si.index));
        map.put("note", mNote.getText().toString());
        map.put("note2", mNote2.getText().toString());
        if (mTourplan.isChecked())
            map.put("istourplan", "1");
        if (mOrder.isChecked())
            map.put("isorder", "1");

        ImageApplication.isPostRunning = true;
        setThreadControls();
        new ImageUploadPostThread(Settings.URL_BASE + Settings.URL_END_ADD, map, ImageApplication.currentPhotoPath).start();
    }

    private void updateImage() {
        Bitmap bmp = ImageUploadPostThread.decodeBitmap();
        if (bmp != null) {
            mImage.setImageBitmap(bmp);

        } else {
            mImage.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_TAKE_PHOTO)
            return;
        if (resultCode != RESULT_OK) {
            ImageApplication.currentPhotoPath = null;
        }
        updateImage();
        updateButtons();
     }

    private static File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir =  new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ),
                    "CameraBosh")            ;

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        return null;
                    }
                }
            }

        } else {
            return null;
        }

        return storageDir;
    }

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private static File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        ImageApplication.currentPhotoPath = f.getAbsolutePath();

        return f;
    }

    protected void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = null;
        try {
            file = setUpPhotoFile();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, RC_TAKE_PHOTO);
    }

    public void onPostFinished() {
        ImageApplication.isPostRunning = false;
        setThreadControls();
    }
    public void updateButtons() {
        if (ImageApplication.currentPhotoPath == null || ImageApplication.isPostRunning || mShop.getSelectedItemPosition() < 1) {
            mUploadButton.setEnabled(false);
            mSaveButton.setEnabled(false);
        } else {
            mUploadButton.setEnabled(true);
            mSaveButton.setEnabled(true);
        }

    }
}
