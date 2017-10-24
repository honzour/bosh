package cz.bosh.imageupload;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Map;


/**
 * Created by honza on 27.9.17.
 */

public class CsvDownloadPostThread extends Thread {

    protected String mUrl;
    protected Map<String, String> mPostArgs;
    protected Handler mHandler;

    public CsvDownloadPostThread(String url, Map<String, String> postArgs) {
        super();
        mUrl = url;
        mPostArgs = postArgs;
        mHandler = new Handler();
    }

    protected String doPost() throws Exception {

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
        if (responseCode != 200) {
            throw new RuntimeException(ImageApplication.imageApplication.getResources().getText(R.string.server_response) + String.valueOf(responseCode));
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        boolean first = true;

        while ((inputLine = in.readLine()) != null) {
            if (first) {
                first = false;
            } else {
                response.append("\n");
            }
            response.append(inputLine);
        }
        in.close();


        return response.toString();

    }

    @Override
    public void run() {
        String error = null;
        String result = null;
        try {
             result = doPost();
        } catch (Exception e) {
            error = e.getMessage();
            if (error == null) {
                error = "Chyba";
            }
        }
        final String errorFinal = error;
        final String resultFinal = result;

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (errorFinal == null) {

                    final SharedPreferences prefs = ImageApplication.imageApplication.getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);

                    final SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(Settings.PREFS_CSV, resultFinal);
                    edit.commit();
                    ImageApplication.parseCsv(resultFinal);

                    if (ImageApplication.mainActivity != null) {
                        ImageApplication.mainActivity.onPostFinished();
                    }
                }
                else {
                    Toast.makeText(ImageApplication.imageApplication, errorFinal, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
