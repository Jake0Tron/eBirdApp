package com.jakedeacon.jake.maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jake on 2/19/2016.
 * Handles URL Construction for eBird API
 */
public class URLBuilder {

    private String url;

    // get the URL for nearby sightings based on:
    //  @param  Lat/lng
    //  @param  Distance from location (radius in km)
    //  @param  number of days prior
    //  https://confluence.cornell.edu/display/CLOISAPI/eBird-1.1-RecentNearbyObservations#eBird-1.1-RecentNearbyObservations-JSON
    public String getNearbySightingsURL(LatLng location, int radius, int daysPrior){

        // handle data limits
        if (radius > 50)
            radius = 50;
        else if (radius < 1 )
            radius = 1;

        if (daysPrior > 30)
            daysPrior = 30;
        else if (daysPrior < 1)
            daysPrior = 1;

        // URL format:
        //http://ebird.org/ws1.1/data/obs/geo/recent?lng=-76.51&lat=42.46&dist=2&back=5&maxResults=500&locale=en_US&fmt=json

        this.url = "http://ebird.org/ws1.1/data/obs/geo/recent?";
        //lat
        this.url += "lat=" + String.valueOf(location.latitude);
        // lng
        this.url += "&lng=" + String.valueOf(location.longitude);
        // radius
        this.url += "&dist=" + String.valueOf(radius);
        //days prior
        this.url += "&back=" + String.valueOf(daysPrior);
        // request JSON
        this.url += "&hotspot=false&includeProvisional=true&locale=en_US&fmt=json";

        return this.url;
    }

    public URLBuilder(){
        this.url = "";
    }

}
