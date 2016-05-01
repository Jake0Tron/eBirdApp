package com.jakedeacon.jake.maps;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Jake Deacon
 * @version 1.0
 *          This class is designed to handle all bird location GET HttpRequests made by the application.
 *          Code sourced and altered from http://hmkcode.com/android-parsing-json-data/
 * @since 2016-02-19
 */
public class BirdSightingAsyncTask extends AsyncTask<String, Integer, JSONArray> {

    public AsyncBirdSightingResponse delegate = null;

    public BirdSightingAsyncTask(AsyncBirdSightingResponse delegate) {
        this.delegate = delegate;
    }

    public BirdSightingAsyncTask() {
        this.delegate = null;
    }

    public void setDelegate(AsyncBirdSightingResponse de) {
        this.delegate = de;
    }

    // BEFORE execute
    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate();
    }


    // Receives a URL to return JSON Object
    // http.execute(url);
    @Override
    protected JSONArray doInBackground(String... urls) {
        String get;
        get = AsyncHelper.GET(urls[0]);
        publishProgress();
        if (get != null) {
            JSONArray json = null;
            try {
                json = new JSONArray(get);
            } catch (JSONException e) {
            }

            return json;
        } else {
            return null;
        }
    }

    // http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

    // POST execute will call processFinish() and pass a JSON object back to activity to be used
    // TO RECEIVE DATA: implement the AsyncBirdSightingResponse interface and the processFinish method
    @Override
    protected void onPostExecute(JSONArray result) {
        delegate.sightingProcessFinish(result);
    }


}

