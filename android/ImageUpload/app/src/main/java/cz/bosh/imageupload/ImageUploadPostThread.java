package cz.bosh.imageupload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;


/**
 * Created by honza on 15.5.17.
 */

public class ImageUploadPostThread extends Thread {

    protected String mUrl;
    protected Map<String, String> mPostArgs;
    protected String mPathToImage;
    protected Handler mHandler;

    public ImageUploadPostThread(String url, Map<String, String> postArgs, String pathToImage) {
        super();
        mUrl = url;
        mPostArgs = postArgs;
        mPathToImage = pathToImage;
        mHandler = new Handler();
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

    public static Bitmap decodeBitmap() throws InterruptedException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        final int maxWidth = 640;
        final int maxHeight = 480;

        do {
            BitmapFactory.decodeFile(ImageApplication.currentPhotoPath, options);
            if (options.outWidth < 0)
                Thread.sleep(100);
        }
        while (options.outWidth < 0);

        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;

        Bitmap smaller_bm = BitmapFactory.decodeFile(ImageApplication.currentPhotoPath, options);
        return smaller_bm;
    }

    protected String doPostWithImage() {

        try {
            Bitmap smaller_bm = decodeBitmap();

            File f = new File(mPathToImage);
            String name = f.getName();
            f.delete();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            smaller_bm.compress(Bitmap.CompressFormat.JPEG, 80, out);
            smaller_bm.recycle();

            byte[] b = out.toByteArray();


            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            URLConnection connection = new URL(mUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            final String charset = "UTF-8";

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);


            // Send normal params
            for (Map.Entry<String, String> entry : mPostArgs.entrySet()) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).append(entry.getValue()).append(CRLF).flush();
            }

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + name + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(name)).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();


            InputStream input = new ByteArrayInputStream(b);

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
            String responseMessage = ((HttpURLConnection) connection).getResponseMessage();

            return  ImageApplication.imageApplication.getResources().getText(R.string.server_response).toString() + String.valueOf(responseCode) + " " + responseMessage;
        } catch (Exception e) {
            return  e.toString();
        }

    }

    @Override
    public void run() {
        final String result = doPostWithImage();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImageApplication.imageApplication, result, Toast.LENGTH_LONG).show();
                if (ImageApplication.imageActivity != null) {
                    ImageApplication.imageActivity.onPostFinished();
                }
            }
        });
    }
}
