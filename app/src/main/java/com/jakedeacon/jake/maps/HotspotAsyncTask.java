package com.jakedeacon.jake.maps;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * @author Jake Deacon
 * @version 1.1
 *          Handles all HttpRequests needed for Hotspots provided from eBird API
 * @since 2016-04-28
 */
public class HotspotAsyncTask extends AsyncTask<String, Integer, JSONObject> {
    public AsyncHotspotResponse delegate;

    public HotspotAsyncTask() {
        this.delegate = null;
    }

    public void setDelegate(AsyncHotspotResponse de) {
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
    protected JSONObject doInBackground(String... urls) {
        String get;
        get = AsyncHelper.GET(urls[0]);
        publishProgress();
        if (get != null) {
            JSONObject json = null;
            try {
                json = XML.toJSONObject(get);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        delegate.hotspotProcessFinish(result);
    }

}
