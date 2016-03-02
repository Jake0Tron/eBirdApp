package com.jakedeacon.jake.maps;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Jake on 2/19/2016.
 * Interface that can be implemented to handle Async Response returns
 */
public interface AsyncResponse {
    void processFinish(JSONArray output);
}
