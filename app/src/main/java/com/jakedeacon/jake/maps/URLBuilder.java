package com.jakedeacon.jake.maps;

import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Handles URL Construction for eBird API calls
 *
 * @author Jake Deacon
 * @version 1.0
 * @since 2016-02-19
 */
public class URLBuilder {

    private String url;

    /**
     * Get the URL for nearby sightings based on:
     *
     * @param location  - Lat/lng
     * @param radius    Distance from location (radius in km)
     * @param daysPrior number of days prior
     * @return URL to query for JSON list of hotspots
     * https://confluence.cornell.edu/display/CLOISAPI/eBird-1.1-RecentNearbyObservations#eBird-1.1-RecentNearbyObservations-JSON
     */
    public String getNearbySightingsURL(LatLng location, int radius, int daysPrior) {

        // handle data limits
        if (radius > 50)
            radius = 50;
        else if (radius < 1)
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

    /**
     * Get the URL for nearby Hotspots based on:
     *
     * @param location  - Lat/lng
     * @param radius    Distance from location (radius in km)
     * @param daysPrior number of days prior
     * @return URL to query for JSON list of hotspots
     */
    public String getHotspotURL(LatLng location, int radius, int daysPrior) {
// handle data limits
        if (radius > 50)
            radius = 50;
        else if (radius < 1)
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

    /**
     * Create URL to query for list of all eBird Scientific names
     *
     * @return URL to list of all eBird scientific names
     */
    public String getListOfAlleBirds() {

        // At this point only english names will be returned...
        // translation to be addressed at a later date.

        // XML string if needed
        // http://ebird.org/ws1.1/ref/taxa/ebird?cat=species,spuh&fmt=XML&locale=en_US
        this.url = "http://ebird.org/ws1.1/ref/taxa/ebird?cat=domestic,form,hybrid,intergrade,issf,slash,species,spuh&fmt=json&locale=en_US";

        return url;
    }

    /**
     * @param sciName        - Scientific name to request server for
     * @param myLatLng       - location to provide in URL
     * @param radiusValue    - distance from location to provide in URL
     * @param daysPriorValue - how many days previous to request in URL
     * @return URL to query for list of specific species sightings
     */
    public String getNearbySpecificSightings(String sciName, LatLng myLatLng, int radiusValue, int daysPriorValue) {

        // handle data limits
        if (radiusValue > 50)
            radiusValue = 50;
        else if (radiusValue < 1)
            radiusValue = 1;

        if (daysPriorValue > 30)
            daysPriorValue = 30;
        else if (daysPriorValue < 1)
            daysPriorValue = 1;

        // http://ebird.org/ws1.1/data/obs/geo_spp/recent?lng=-76.51&lat=42.46&sci=branta%20canadensis&dist=2&back=5&maxResults=500&locale=en_US&fmt=json


        // encode any species query
        String name = null;
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

    public URLBuilder() {
        this.url = "";
    }
}
