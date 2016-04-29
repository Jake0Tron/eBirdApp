package com.jakedeacon.jake.maps;

import java.util.ArrayList;

/**
 * Created by User on 4/29/2016.
 * Interface built to handle scientific bird name response
 */
public interface AsyncSciNameResponse {
    void sciNameProcessFinish(ArrayList<String> output);
}
