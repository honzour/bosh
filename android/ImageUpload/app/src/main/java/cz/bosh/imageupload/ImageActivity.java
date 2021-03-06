package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageActivity extends Activity {

    public static final String INTENT_EXTRA_RECORD = "INTENT_EXTRA_RECORD";
    public static final String INTENT_EXTRA_ID = "INTENT_EXTRA_ID";

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
    private View mDeleteButton;
    private View mProgressBar;
    private Spinner mShop;
    private ImageView mImage;
    private CheckBox mUseGps;
    private CheckBox mTourplan;
    private CheckBox mOrder;

    private List<SelectItem> mShopData;
    private Database.Record mRecord;
    private Database.ShortRecord mShortRecord;

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
        if (getIntent().getExtras() != null) {
            mRecord = (Database.Record) getIntent().getExtras().getSerializable(INTENT_EXTRA_RECORD);
            mShortRecord = (Database.ShortRecord)getIntent().getExtras().getSerializable(INTENT_EXTRA_ID);
        }
        setContentView(R.layout.image);

        mUploadButton = findViewById(R.id.image_upload_button);
        mSaveButton = findViewById(R.id.image_save_button);
        mDeleteButton = findViewById(R.id.image_delete_button);

        mAccuracy = (TextView) findViewById(R.id.image_accuracy);
        mNote = (TextView) findViewById(R.id.image_note);
        mNote2 = (TextView) findViewById(R.id.image_note2);
        mProgressBar = findViewById(R.id.image_progress);
        mShop = (Spinner) findViewById(R.id.image_shop);
        mImage = (ImageView) findViewById(R.id.image_image);
        mUseGps = (CheckBox) findViewById(R.id.image_use_gps);
        mTourplan = (CheckBox) findViewById(R.id.image_tourplan);
        mOrder = (CheckBox) findViewById(R.id.image_order);


        if (mRecord == null) {
            updateImage();

            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCamera();
                }
            });
        }

        List<String> csv = ImageApplication.getCsv();

        List<String> list = new ArrayList<String>(csv.size() + 1);
        mShopData = new ArrayList<SelectItem>(list.size() + 1);

        mShopData.add(null);
        list.add("----------------------------");

        for (String line : csv) {
            SelectItem si = null;
            try {
                String[] fields = line.split("\\$");
                line = fields[0] + ',' + fields[2] + ',' + fields[1];
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

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.Record record = createDatabaseRecord();
                ImageApplication.database.insert(record);
                finish();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageApplication.database.delete(mShortRecord.id);
                finish();
            }
        });

        if (mRecord != null) {
            fillDatabaseRecord();
            mUseGps.setChecked(false);
            mUseGps.setEnabled(false);
            //mNote.setEnabled(false);
            //mNote2.setEnabled(false);

            mNote.setInputType(InputType.TYPE_NULL);
            mNote2.setInputType(InputType.TYPE_NULL);

            mShop.setEnabled(false);
            mTourplan.setEnabled(false);
            mOrder.setEnabled(false);
        }

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
        if (mRecord == null) {
            if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(ImageActivity.this, R.string.no_gps, Toast.LENGTH_LONG).show();
            } else {
                // TODO min time
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
            }
        }
    }

    protected Database.Record createDatabaseRecord() {
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

        File f = new File(ImageApplication.currentPhotoPath);
        String name = f.getName();
        Database.Record record = new Database.Record(name, getImage(), map);
        return record;
    }

    protected void fillDatabaseRecord() {
        mNote.setText(mRecord.map.get("note"));;
        mNote2.setText(mRecord.map.get("note2"));;
        mTourplan.setChecked(mRecord.map.containsKey("istourplan"));
        mOrder.setChecked(mRecord.map.containsKey("isorder"));
        try {
            int shop = Integer.valueOf(mRecord.map.get("shop"));
            for (int i = 0; i < mShopData.size(); i++) {
                if (mShopData.get(i) == null) continue;
                if (mShopData.get(i).index == shop) {
                    mShop.setSelection(i);
                    break;
                }
            }
        } catch (Exception e) {
            mShop.setSelection(0);
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(mRecord.image, 0, mRecord.image.length);
        mImage.setImageBitmap(bmp);
    }

    protected void prepareAndStartPost() {
        ImageApplication.isPostRunning = true;
        setThreadControls();
        new ImageUploadPostThread(Settings.URL_BASE + Settings.URL_END_ADD, mRecord == null ? createDatabaseRecord() : mRecord).start();
    }

    protected static byte[] getImage() {
        Bitmap smaller_bm = decodeBitmap();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        smaller_bm.compress(Bitmap.CompressFormat.JPEG, 80, out);
        smaller_bm.recycle();

        return out.toByteArray();
    }

    public static Bitmap decodeBitmap() {
        if (ImageApplication.currentPhotoPath == null)
            return null;
        Bitmap smaller_bm = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            final int maxWidth = 640;
            final int maxHeight = 480;

            int iter = 0;
            do {
                BitmapFactory.decodeFile(ImageApplication.currentPhotoPath, options);
                if (options.outWidth < 0)
                    if (iter > 5)
                        throw new RuntimeException("Cannot read photo");
                Thread.sleep(100);
                iter++;
            }
            while (options.outWidth < 0);

            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;

            smaller_bm = BitmapFactory.decodeFile(ImageApplication.currentPhotoPath, options);
        } catch (Exception e) {
            // ignore, use null value
        }
        if (smaller_bm == null) {
            ImageApplication.currentPhotoPath = null;
        }
        return smaller_bm;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }



    private void updateImage() {
        Bitmap bmp = decodeBitmap();
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
            mUploadButton.setEnabled(mRecord != null);
            mSaveButton.setEnabled(false);
        } else {
            mUploadButton.setEnabled(true);
            mSaveButton.setEnabled(mRecord == null);
        }
        mDeleteButton.setEnabled(mRecord != null && !ImageApplication.isPostRunning);
    }
}
