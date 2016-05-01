package com.jakedeacon.jake.maps;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Jake Deacon
 * @version 1.1
 *          Handles loading scientific bird names for auto complete box in MainActivity.class
 *          Handles loading progress display as bird names are loaded and parsed into ArrayList
 * @since - 4/29/2016.
 */
public class AsyncSciNameTask extends AsyncTask<String, Integer, ArrayList<String>> {

    public AsyncSciNameResponse delegate = null;

    ProgressBar progress;
    WeakReference<Activity> weakActivity;
    Activity act;

    public AsyncSciNameTask(Activity activity) {
        this.delegate = null;
        // get reference to activity to update UI as needed for progress
        weakActivity = new WeakReference<>(activity);
    }

    public void setDelegate(AsyncSciNameResponse de) {
        this.delegate = de;
    }
    // LOGIC 1
    // returns JSON String retrieved from provided URL


    // BEFORE execute
    @Override
    protected void onPreExecute() {
        act = weakActivity.get();
        if (act != null) {
            this.progress = (ProgressBar) act.findViewById(R.id.main_menu_progress);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        super.onProgressUpdate();
        act = weakActivity.get();
        if (act != null) {
            this.progress = (ProgressBar) act.findViewById(R.id.main_menu_progress);
            this.progress.setEnabled(true);
            this.progress.setVisibility(View.VISIBLE);
            this.progress.setProgress(progress[0]);
        }
    }

    /**
     * Overridden method that handles the request and parsing of scientific name data from eBird
     * server
     *
     * @param urls - Array of URLs to be handled (ONLY HANDLES 1) and queried.
     * @return - ArrayList of strings that contain the scientific names provided by server
     */
    @Override
    protected ArrayList<String> doInBackground(String... urls) {
        String get;
        get = AsyncHelper.GET(urls[0]);
        ArrayList<String> sciNames = new ArrayList<>();
        if (get != null) {
            JSONArray json;
            try {
                json = new JSONArray(get);

                // parse JSON and pass back ArrayList<String> to return
                for (int i = 0; i < json.length(); i++) {
                    // go through JArray
                    JSONObject species = json.getJSONObject(i);
                    // get sci name from object
                    String sciName = species.getString("sciName");
                    // add to list
                    sciNames.add(sciName);
                    // update bar
                    publishProgress((int) ((i / (float) json.length()) * 100));
                }
                // sort alphabetically
                Collections.sort(sciNames, String.CASE_INSENSITIVE_ORDER);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return sciNames;
        } else {
            return null;
        }
    }

    // http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

    // POST execute will call processFinish() and pass a JSON object back to activity to be used
    // TO RECEIVE DATA: implement the AsyncBirdSightingResponse interface and the processFinish method
    @Override
    protected void onPostExecute(ArrayList<String> result) {
        delegate.sciNameProcessFinish(result);
    }

}
