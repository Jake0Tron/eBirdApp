package com.jakedeacon.jake.maps;

import org.json.JSONObject;

/**
 * Created by Jake on 2/19/2016.
 * Interface that can be implemented to handle Async Response returns
 */
public interface AsyncHotspotResponse {
    void hotspotProcessFinish(JSONObject output);
}
