package com.jakedeacon.jake.maps;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by User on 4/28/2016.
 */
public class HotspotAsyncTask extends AsyncTask<String, Integer, JSONObject> {
    public AsyncHotspotResponse delegate;

    public HotspotAsyncTask(){this.delegate = null;}

    public void setDelegate (AsyncHotspotResponse de){ this.delegate = de;}

    // LOGIC 1
    // returns JSON String retrieved from provided URL
    public static String GET(String url){
        String result = "";

        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection urlConnection = null;
        if (u != null){
            try {
                urlConnection = (HttpURLConnection) u.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (urlConnection != null){
                InputStream in = null;
                try {
                    in = new BufferedInputStream(urlConnection.getInputStream());
                }catch (Exception e){e.printStackTrace();}
                if (in != null){
                    result = convertInputStreamToString(in);
                }
                else{
                    return null;
                }

                urlConnection.disconnect();
            }
        }
        return result;
    }

    // LOGIC 2 - Type Conversion from GET
    public static String convertInputStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        try {
            while((line = bufferedReader.readLine()) != null)
                result += line;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    // BEFORE execute
    @Override
    protected void onPreExecute(){

    }

    @Override
    protected void onProgressUpdate(Integer... progress){
        super.onProgressUpdate();
    }


    // Receives a URL to return JSON Object
    // http.execute(url);
    @Override
    protected JSONObject doInBackground(String... urls) {
        String get;
        get = GET(urls[0]);
        publishProgress();
        if (get != null){
            JSONObject json = null;
            Log.d("hotspotXML", get);
            try {
                json = XML.toJSONObject(get);
//                json = new JSONArray(get);
            } catch (JSONException e) {}

            return json;
        }else{
            return null;
        }
    }

    // http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

    // POST execute will call processFinish() and pass a JSON object back to activity to be used
    // TO RECEIVE DATA: implement the AsyncBirdSightingResponse interface and the processFinish method
    @Override
    protected void onPostExecute(JSONObject result){
        delegate.hotspotProcessFinish(result);
    }

}
