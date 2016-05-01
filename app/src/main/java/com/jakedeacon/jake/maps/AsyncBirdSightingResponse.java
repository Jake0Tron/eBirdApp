package com.jakedeacon.jake.maps;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Jake on 2/19/2016.
 * Interface that can be implemented to handle Async bird sighting value returns
 */
public interface AsyncBirdSightingResponse {
    void sightingProcessFinish(JSONArray output);
}
