package com.jakedeacon.jake.maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    public String getHotspotURL(LatLng location, int radius, int daysPrior){
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
        //http://ebird.org/ws1.1/ref/hotspot/geo?lng=-76.51&lat=42.46&dist=2&back=5&fmt=json

        this.url = "http://ebird.org/ws1.1/ref/hotspot/geo?";
        //lat
        this.url += "lat=" + String.valueOf(location.latitude);
        // lng
        this.url += "&lng=" + String.valueOf(location.longitude);
        // radius
        this.url += "&dist=" + String.valueOf(radius);
        //days prior
        this.url += "&back=" + String.valueOf(daysPrior);
        // request JSON
        this.url += "&hotspot=false&includeProvisional=true&locale=en_US&fmt=xml";

        return this.url;
    }

    public String getListOfAlleBirds(){

        // At this point only english names will be returned...
        // translation to be addressed at a later date.

        // XML string if needed
        // http://ebird.org/ws1.1/ref/taxa/ebird?cat=species,spuh&fmt=XML&locale=en_US
        this.url = "http://ebird.org/ws1.1/ref/taxa/ebird?cat=species,spuh&fmt=json&locale=en_US";

        return url;
    }

    public String getNearbySpecificSightings(String sciName, LatLng myLatLng, int radiusValue, int daysPriorValue){

        // handle data limits
        if (radiusValue > 50)
            radiusValue = 50;
        else if (radiusValue < 1 )
            radiusValue = 1;

        if (daysPriorValue > 30)
            daysPriorValue = 30;
        else if (daysPriorValue < 1)
            daysPriorValue = 1;

        // http://ebird.org/ws1.1/data/obs/geo_spp/recent?lng=-76.51&lat=42.46&sci=branta%20canadensis&dist=2&back=5&maxResults=500&locale=en_US&fmt=json


        // encode any species query
        String name= null;
        try {
            name = URLEncoder.encode(sciName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.url = "http://ebird.org/ws1.1/data/obs/geo_spp/recent?";
        this.url += "lng=" + String.valueOf(myLatLng.longitude);
        this.url += "&lat=" + String.valueOf(myLatLng.latitude);
        this.url += "&sci=" + name;
        this.url += "&dist=" + radiusValue;
        this.url += "&back=" + daysPriorValue;
        this.url += "&locale=en_US&fmt=json";

        return url;
    }

    public URLBuilder(){
        this.url = "";
    }

}
