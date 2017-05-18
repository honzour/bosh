package cz.bosh.imageupload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class PostThread extends Thread {

    protected String mUrl;
    protected Map<String, String> mPostArgs;
    protected String mPathToImage;
    protected Handler mHandler;

    public PostThread(String url, Map<String, String> postArgs, String pathToImage) {
        super();
        mUrl = url;
        mPostArgs = postArgs;
        mPathToImage = pathToImage;
        mHandler = new Handler();
    }

    public int calculateInSampleSize(
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

    protected String doPostWithImage() {

        try {


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            final int maxWidth = 640;
            final int maxHeight = 480;

            BitmapFactory.decodeFile(mPathToImage, options);

            if (options.outWidth > maxWidth || options.outHeight > maxHeight) {

                options.inSampleSize = calculateInSampleSize(options, 640, 480);
                options.inJustDecodeBounds = false;

                Bitmap smaller_bm = BitmapFactory.decodeFile(mPathToImage, options);
                new File(mPathToImage).delete();
                File small_picture = new File(mPathToImage);
                FileOutputStream fOut = new FileOutputStream(small_picture);

                smaller_bm.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                fOut.flush();
                fOut.close();
                smaller_bm.recycle();
            }

            File binaryFile = new File(mPathToImage);

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
            String responseMessage = ((HttpURLConnection) connection).getResponseMessage();
            new File(mPathToImage).delete();

            return  ImageApplication.imageApplication.getResources().getText(R.string.server_response).toString() + String.valueOf(responseCode) + " " + responseMessage ;
        } catch (Exception e) {
            return  e.toString();
        }

    }


    protected String doPost() {

        try {

            URL obj = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Muj Browser");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            StringBuffer urlParameters = new StringBuffer();
            for (Map.Entry<String, String> entry : mPostArgs.entrySet()) {

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


             return ImageApplication.imageApplication.getResources().getText(R.string.server_response) + String.valueOf(responseCode);

        }
        catch (Exception e) {
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
                if (ImageApplication.mainActivity != null) {
                    ImageApplication.mainActivity.onPostFinished();
                }
            }
        });
    }
}
