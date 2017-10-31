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
//    protected Map<String, String> mPostArgs;
//    protected String mPathToImage;
    Database.Record mRecord;
    protected Handler mHandler;

    public ImageUploadPostThread(String url, /*Map<String, String> postArgs, String pathToImage*/ Database.Record record) {
        super();
        mUrl = url;
        mRecord = record;
        //mPostArgs = postArgs;
        //mPathToImage = pathToImage;
        mHandler = new Handler();
    }



    protected String doPostWithImage() {

        try {

            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            URLConnection connection = new URL(mUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            final String charset = "UTF-8";

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);


            // Send normal params
            for (Map.Entry<String, String> entry : mRecord.map.entrySet()) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).append(entry.getValue()).append(CRLF).flush();
            }

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + mRecord.filename + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(mRecord.filename)).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();


            InputStream input = new ByteArrayInputStream(mRecord.image);

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

            if (responseCode == 200) {
                ImageApplication.currentPhotoPath = null;
                return null;
            }
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

                if (ImageApplication.imageActivity != null) {
                    ImageApplication.imageActivity.onPostFinished();
                }

                if (result == null) {
                    if (ImageApplication.imageActivity != null) {
                        Toast.makeText(ImageApplication.imageApplication, "Image uploaded", Toast.LENGTH_LONG).show();
                        ImageApplication.imageActivity.finish();
                    }
                } else {
                    Toast.makeText(ImageApplication.imageApplication, result, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
