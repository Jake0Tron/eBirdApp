package com.jakedeacon.jake.maps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles map activity with relation to search parameters passed in from MainMenuActivity
 *
 * @param species - optionally provided scientific name handed in through intent.putExtra()
 *                If empty, all species will be searched for.
 * @author Jake Deacon
 * @version 1.1
 * @since 2016-04-31
 */

@SuppressWarnings("ALL")
public class SightingsNearMeActivity
        extends FragmentActivity
        implements AsyncBirdSightingResponse, AsyncHotspotResponse, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private String provider;
    private LatLng myLatLng;
    private Location myLocation;
    private float lat, lon;

    // map camera values
    private int maxZoom, minZoom, defZoom, tiltValue;
    // idle camera position
    private CameraPosition campos;

    // map marker for user
    Marker myMarker;
    MarkerOptions myMarkerOptions;

    // radius circle
    private Circle myCircle;
    private CircleOptions circleOptions;
    // visible radius
    private int radiusValue;

    // number of days prior
    private int daysPriorValue;

    // Number pickers
    // Radius
    private NumberPicker radiusPicker;
    // days prior
    private NumberPicker daysPriorPicker;

    // URL Builder
    private URLBuilder uBuilder;

    // sightTask request
    private BirdSightingAsyncTask sightTask;

    // list of results from request
    private ArrayList<MarkerOptions> sightingResultList;
    // list of markers created after JSON received
    private ArrayList<MarkerOptions> sightingMarkers;
    // list of titles from JSON
    private ArrayList<String> matchingBirdTitles;
    // list of subtitles from JSON
    private ArrayList<String> matchingBirdSubTitles;
    // alert dialog for multiple birds
    private AlertDialog multiBirdAlert;
    // Spinner to handle multiple birds in the alertdialog
    private Spinner multiBirdSpinner;

    // Follow Toggle
    private ToggleButton followToggle;

    // list of markers for hotspots
    private ArrayList<MarkerOptions> hotspotMarkers;

    private Context currentContext;
    // boolean indicating whether or not species-specific search is being used
    boolean specSpec;

    //species to search for
    String searchSpecies;

    // Hotspot task request
    private HotspotAsyncTask hotTask;
    // list of titles of matching hotspots
    ArrayList<String> matchingHotspotTitles;
    ArrayList<String> matchingHotspotSubTitles;
    // Map of markers/string ID's that determine which marker is being clicked on Marker Click
    HashMap<Marker, String> markerTags;

    // DEBUG
    private String TAG = "eBirdSightings";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sightings_near_me);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        myLocation = new Location(provider);
        myLocation = locationManager.getLastKnownLocation(provider);

        this.currentContext = this;

        // camera zoom values
        this.maxZoom = 30;  // close
        this.defZoom = 12;
        this.minZoom = 4;  // far
        this.radiusValue = 5;
        this.daysPriorValue = 25;

        this.followToggle = (ToggleButton) findViewById(R.id.toggleFollow);
        this.followToggle.setChecked(true);

        Intent i = getIntent();
        this.searchSpecies = i.getStringExtra("species");
        if (searchSpecies.equals("")) {
            // no specific species, find all
            specSpec = false;
        } else {
            // find specifoc species
            specSpec = true;
        }

        // get number pickers
        daysPriorPicker = (NumberPicker) findViewById(R.id.daysPriorPicker);
        daysPriorPicker.setMinValue(1);
        daysPriorPicker.setMaxValue(30);
        daysPriorPicker.setValue(daysPriorValue);
        daysPriorPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    daysPriorValue = view.getValue();
                    getBirdsNearMe();
                    getHotspotsNearMe();
                }
            }
        });

        radiusPicker = (NumberPicker) findViewById(R.id.radiusPicker);
        radiusPicker.setMinValue(1);
        radiusPicker.setMaxValue(30);
        radiusPicker.setValue(radiusValue);
        radiusPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    radiusValue = view.getValue();
                    myCircle.remove();
                    drawViewRadius();
                    getBirdsNearMe();
                    getHotspotsNearMe();
                }
            }
        });

        sightingResultList = new ArrayList<MarkerOptions>();
        sightingMarkers = new ArrayList<MarkerOptions>();
        hotspotMarkers = new ArrayList<MarkerOptions>();
        matchingBirdTitles = new ArrayList<String>();
        matchingBirdSubTitles = new ArrayList<String>();
        matchingHotspotTitles = new ArrayList<String>();
        matchingHotspotSubTitles = new ArrayList<String>();
        markerTags = new HashMap<Marker, String>();

        this.uBuilder = new URLBuilder();

        // Initialize the location fields
        if (myLocation != null) {
            onLocationChanged(myLocation);
        } else {
            Toast.makeText(this, R.string.error_gps_enable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        this.myLatLng = new LatLng(lat, lon);
        drawMyLocation();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLatLng)
                .zoom(defZoom)
                .tilt(tiltValue)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // handle camera change
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.getUiSettings().setScrollGesturesEnabled(!followToggle.isChecked());
                float curZoomVal = cameraPosition.zoom;
                float curBearing = cameraPosition.bearing;
                float curTilt = cameraPosition.tilt;

                // if not following, allow camera to move
                if (!followToggle.isChecked()) {
                    campos = cameraPosition;

                    drawMyLocation();

                    // Camera Limits
                    if (cameraPosition.zoom >= maxZoom) {
                        CameraPosition camPos = new CameraPosition.Builder()
                                .target(myLatLng)      // Sets the center of the map to my location
                                .zoom(maxZoom - 0.01f) // Sets the zoom
                                .bearing(curBearing)   // Sets the orientation of the camera to east
                                .tilt(curTilt)         // Sets the tilt of the camera to tilt value
                                .build();              // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    } else if (cameraPosition.zoom <= minZoom) {
                        CameraPosition camPos = new CameraPosition.Builder()
                                .target(myLatLng)      // Sets the center of the map to me
                                .zoom(minZoom + 0.01f) // Sets the zoom
                                .bearing(curBearing)   // Sets the orientation of the camera to east
                                .tilt(curTilt)         // Sets the tilt of the camera to 30 degrees
                                .build();              // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    } else {
                        // lock camera on user
                        CameraPosition camPos = new CameraPosition.Builder()
                                .target(myLatLng)      // Sets the center of the map to me
                                .zoom(curZoomVal)      // Sets the zoom
                                .bearing(curBearing)   // Sets the orientation of the camera to east
                                .tilt(curTilt)         // Sets the tilt of the camera to 30 degrees
                                .build();              // Creates a CameraPosition from the builder
                    }
                } else {
                    // animate camera to follow myLatLng
                    cameraPosition = new CameraPosition.Builder()
                            .target(myLatLng)
                            .zoom(curZoomVal)
                            .tilt(curTilt)
                            .bearing(curBearing)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }// end onCameraChange
        });// end onCameraChangeListener

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (markerTags.get(marker) == "bird") {
                    matchingBirdTitles.clear();
                    matchingBirdSubTitles.clear();

                    for (int i = 0; i < sightingResultList.size(); i++) {

                        double markerLat = sightingResultList.get(i).getPosition().latitude;
                        double markerLon = sightingResultList.get(i).getPosition().longitude;

                        double clickLat = marker.getPosition().latitude;
                        double clickLon = marker.getPosition().longitude;

                        String matchingBirdDataTitle = sightingResultList.get(i).getTitle();
                        String matchingBirdDataSubTitle = sightingResultList.get(i).getSnippet();

                        if ((markerLat == clickLat) && (markerLon == clickLon)) {
                            matchingBirdTitles.add(matchingBirdDataTitle);
                            matchingBirdSubTitles.add(matchingBirdDataSubTitle);
                        }
                    }

                    if (matchingBirdTitles.size() > 0) {
                        LayoutInflater inflater = SightingsNearMeActivity.this.getLayoutInflater();
                        AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                        String titleString = getResources().getString(R.string.sighting_near_me_alert_title) + " " +
                                String.valueOf(matchingBirdTitles.size());

                        adb.setView(inflater.inflate(R.layout.alert_dialog_multiple_sightings_near_me, null))
                                .setTitle(titleString)
                                .setNegativeButton(R.string.near_me_close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog dialog = adb.create();
                        dialog.show();

                        multiBirdSpinner = (Spinner) dialog.findViewById(R.id.multi_bird_spinner);

                        if (multiBirdSpinner != null) {
                            TextSubTextAdapter adapter
                                    = new TextSubTextAdapter(SightingsNearMeActivity.this,
                                    matchingBirdTitles,
                                    matchingBirdSubTitles);
                            multiBirdSpinner.setAdapter(adapter);
                            multiBirdSpinner.setSelection(0, true);
                        }
                    }
                    return true;
                } else if (markerTags.get(marker) == "hotspot") {
                    matchingHotspotTitles.clear();
                    matchingHotspotSubTitles.clear();

                    for (int i = 0; i < hotspotMarkers.size(); i++) {

                        double markerLat = hotspotMarkers.get(i).getPosition().latitude;
                        double markerLon = hotspotMarkers.get(i).getPosition().longitude;

                        double clickLat = marker.getPosition().latitude;
                        double clickLon = marker.getPosition().longitude;

                        String hotspotTitle = hotspotMarkers.get(i).getTitle();
                        String hotspotSubtitle = hotspotMarkers.get(i).getSnippet();

                        if ((markerLat == clickLat) && (markerLon == clickLon)) {
                            matchingHotspotTitles.add(hotspotTitle);
                            matchingHotspotSubTitles.add(hotspotSubtitle);
                        }
                    }

                    if (matchingHotspotTitles.size() > 0) {
                        LayoutInflater inflater = SightingsNearMeActivity.this.getLayoutInflater();
                        AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                        String titleString = getResources().getString(R.string.hotspot_near_me_alert_title) + " " +
                                String.valueOf(matchingHotspotTitles.size());

                        adb.setView(inflater.inflate(R.layout.alert_dialog_multiple_sightings_near_me, null))
                                .setTitle(titleString)
                                .setNegativeButton(R.string.near_me_close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog dialog = adb.create();
                        dialog.show();

                        multiBirdSpinner = (Spinner) dialog.findViewById(R.id.multi_bird_spinner);

                        if (multiBirdSpinner != null) {
                            TextSubTextAdapter adapter
                                    = new TextSubTextAdapter(SightingsNearMeActivity.this,
                                    matchingHotspotTitles,
                                    matchingHotspotTitles);
                            multiBirdSpinner.setAdapter(adapter);
                            multiBirdSpinner.setSelection(0, true);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });// end markerclickListener

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng touchLatLng) {
                // only allow long press if Follow is disabled
                if (!followToggle.isChecked()) {
                    myLatLng = touchLatLng;
                    getBirdsNearMe();
                    getHotspotsNearMe();
                    drawMyLocation();
                } else {
                    // pop toast to alert user that toggle is on
                    Toast.makeText(currentContext, R.string.near_me_toggle_error, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });// end mapLongClickListener

        getBirdsNearMe();
        getHotspotsNearMe();
    }

    /**
     * getHotspotsNearMe pulls a URL from the URLBuilder class and starts an asynchronous activity
     * to provide locations to draw the markers for all hotspots within the requested radius.
     */
    public void getHotspotsNearMe() {
        String url = uBuilder.getHotspotURL(myLatLng, radiusValue, daysPriorValue);
//        String url = "http://ebird.org/ws1.1/ref/hotspot/geo?lat=42.4613266&lng=-76.5059255&dist=5&back=25&hotspot=false&includeProvisional=true&locale=en_US&fmt=xml";

//        Log.d("hotspotURL", url);
        this.hotTask = new HotspotAsyncTask();
        this.hotTask.setDelegate(this);
        this.hotTask.execute(url);
    }

    /**
     * getBirdsNearMe pulls a URL from the URLBuilder class and starts an asynchronous activity
     * to draw locations for all bird sightings within the requested radius, depending on whether or
     * not the request is for a specific species is requested the URL will be adjusted accordingly.
     */
    public void getBirdsNearMe() {
        String url;
        if (specSpec) {
            url = uBuilder.getNearbySpecificSightings(searchSpecies, myLatLng, radiusValue, daysPriorValue);
        } else {
            url = uBuilder.getNearbySightingsURL(myLatLng, radiusValue, daysPriorValue);
        }

        this.sightTask = new BirdSightingAsyncTask();
        this.sightTask.setDelegate(this);
        this.sightTask.execute(url);
    }

    /**
     * A helper method that calls the camera relocation and location reset methods to re-center the
     * camera as necessary
     *
     * @param v - the button that is clicked on the activity_sightings_near_me.xml that calls this
     *          method
     */
    public void resetLocation(View v) {
        resetMyLocation();
        resetCameraLocation();
    }

    /**
     * Helper methodResets the location used as the user's location after long-pressing the map to
     * view other areas outside of the visible radius.
     */
    public void resetMyLocation() {
        this.myLatLng = new LatLng(lat, lon);
        myLocation = locationManager.getLastKnownLocation(provider);
        getBirdsNearMe();
    }

    /**
     * Helper method that can be used to recenter the camera on the user's current location.
     */
    public void resetCameraLocation() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLatLng)
                .zoom(defZoom)
                .tilt(tiltValue)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Overridden method from the HotspotAsyncTask class that is called as a delegate to allow for
     * data return to the UI thread.
     *
     * @param result - the JSONObject created in the HotspotAsyncTask that provides a list of
     *               hotspots in a requested radius.
     */
    @Override
    public void hotspotProcessFinish(JSONObject result) {
        hotspotMarkers.clear();

        //{
        // "response":
        //  {"result":
        //    {"location":[
        //      {"lng":-76.5146989,"loc-id":"L159024","loc-name":"Allan H. Treman State Marine Park--Marina","lat":42.4585456,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4504712,"loc-id":"L4415110","loc-name":"AviTrail47a - 1 pts","lat":42.4756341,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4489116,"loc-id":"L4415111","loc-name":"AviTrail47b - 2 pts","lat":42.476738,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4577198,"loc-id":"L877944","loc-name":"Bluegrass Lane Natural Area","lat":42.4632963,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4998821,"loc-id":"L1107958","loc-name":"Burdick Hill Rd., Lansing","lat":42.4980885,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.5186596,"loc-id":"L518712","loc-name":"Buttermilk Falls SP (Lower)","lat":42.417659,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.492132,"loc-id":"L1597677","loc-name":"Cascadilla Gorge Trail","lat":42.44298,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.46307,"loc-id":"L157713","loc-name":"Cornell Plantations","lat":42.45136,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4719972,"loc-id":"L290965","loc-name":"Cornell Plantations--Botanical Garden","lat":42.4494592,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4572692,"loc-id":"L799199","loc-name":"Cornell Plantations--F.R. Newman Arboretum","lat":42.4520722,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"},
        //      {"lng":-76.4806044,"loc-id":"L269457","loc-name":"Cornell University--Rockwell Azalea Garden","lat":42.4478844,"country-code":"US","subnational2-code":"US-NY-109","subnational1-code":"US-NY"}...

//        Log.d("hotspotjson", result.toString());

        try {
            JSONObject response = result.getJSONObject("response");
            if (!response.get("result").equals("")) {      // ensure there are results
                JSONObject result1 = response.getJSONObject("result");
                JSONArray locArr = result1.getJSONArray("location");
                for (int i = 0; i < locArr.length(); i++) {

                    //{
                    //  "lng":-76.4806044,                                          0
                    //  "loc-id":"L269457",                                         1
                    //  "loc-name":"Cornell University--Rockwell Azalea Garden",    2
                    //  "lat":42.4478844,                                           3
                    //  "country-code":"US",                                        4
                    // /  "subnational2-code":"US-NY-109",                          5
                    //  "subnational1-code":"US-NY"                                 6
                    // }

                    JSONObject location = locArr.getJSONObject(i);
                    double hotLng = 0.0;
                    double hotLat = 0.0;
                    String hotName = "";
                    String locSnip = "";

                    hotLat = location.getDouble("lat");
                    hotLng = location.getDouble("lng");

                    LatLng locLL = new LatLng(hotLat, hotLng);

                    String displayString = location.getString("loc-name") + " " + location.getString("country-code");
                    locSnip = location.getString("subnational1-code") + " " + String.valueOf(hotLat) + " " + String.valueOf(hotLng);
                    hotName = displayString;

                    MarkerOptions locMarker = new MarkerOptions();
                    locMarker
                            .alpha(0.7f)
                            .draggable(false)
                            .title(hotName)
                            .snippet(locSnip)
                            .position(locLL)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.fire_icon_small));

                    hotspotMarkers.add(locMarker);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        drawHotspotList();
    }

    /**
     * The Overridden Method that is implemented to handle the delegate callback for
     * BirdSightingAsyncTask
     *
     * @param result - THe JSONArray returned from the BirdSightingAsyncTask listing all birds
     *               within a specific radius
     */
    @Override
    public void sightingProcessFinish(JSONArray result) {
        // There is something odd with how eBird filters their results via location and distance from it.
        // after certain distances birds will be removed from the list for reasons I can't tell...
        // Will have to look into this further.

        sightingResultList.clear();
        mMap.clear();

        /*
            [{
                "locID": "L99381",              0
                "lat": 42.4613266,              1
                "howMany": 1,                   2
                "locName": "Stewart Park",      3
                "obsValid": true,               4
                "lng": -76.5059255,             5
                "sciName": "Hirundo rustica",   6
                "obsReviewed": false,           7
                "obsDt": "2009-06-24 17:00",    8
                "comName": "Barn Swallow"       9
            }]
         */
        int displayCount = 0;
        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject sightingJSON = result.getJSONObject(i);
                // create a markeroptions to hold information about bird sighting
                double birdLat = 0.0;
                double birdLong = 0.0;
                String birdComName = "";
                String birdSciName = "";
                String locationName = "";
                String dateSeen = "";
                String birdCount = "";

                try {
                    birdLat = sightingJSON.getDouble("lat");
                } catch (JSONException e) {
                    birdLat = 0.0;
                    e.printStackTrace();
                }

                try {
                    birdLong = sightingJSON.getDouble("lng");
                } catch (JSONException e) {
                    birdLong = 0.0;
                    e.printStackTrace();
                }

                try {
                    birdComName = sightingJSON.getString("comName");
                } catch (JSONException e) {
                    birdComName = "";
                    e.printStackTrace();
                }

                try {
                    birdSciName = sightingJSON.getString("sciName");
                } catch (JSONException e) {
                    birdSciName = "";
                    e.printStackTrace();
                }

                try {
                    locationName = sightingJSON.getString("locName");
                } catch (JSONException e) {
                    locationName = "";
                    e.printStackTrace();
                }

                try {
                    dateSeen = sightingJSON.getString("obsDt");
                } catch (JSONException e) {
                    dateSeen = "";
                    e.printStackTrace();
                }

                try {
                    birdCount = String.valueOf(sightingJSON.getInt("howMany"));
                } catch (JSONException e) {
                    birdCount = "X";
                    e.printStackTrace();
                }

                String markTitle = birdCount + " " + birdComName + " seen at " + locationName;
                String markSnip = birdSciName + " > " + dateSeen + " " + birdLat + " " + birdLong;

                LatLng birdPos = new LatLng(birdLat, birdLong);

                MarkerOptions birdMarker = new MarkerOptions();
                birdMarker
                        .draggable(false)
                        .alpha(0.7f)
                        .title(markTitle)
                        .snippet(markSnip)
                        .position(birdPos)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bird_icon_small));

                sightingResultList.add(birdMarker);

                displayCount++;
            }
            Toast.makeText(this, displayCount + " sightings in this area", Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawMyLocation();
        drawViewRadius();
        drawResultList();
    }

    /**
     * Helper fucntion that is called to draw the user's marker onto the map as needed.
     */
    public void drawMyLocation() {
        if (myMarker != null)
            myMarker.remove();
        myMarkerOptions = new MarkerOptions()
                .position(myLatLng)
                .title("My Location: " + lat + " + " + lon)
                .draggable(false);
        myMarker = mMap.addMarker(myMarkerOptions);
    }

    /**
     * Helper method used to draw the view radius circle which is used to determine how large the
     * radius is when sending requests to BirdSightingAsyncTask
     */
    public void drawViewRadius() {
        this.circleOptions = new CircleOptions()
                .center(myLatLng)
                .strokeWidth(1.5f)
                .radius((radiusValue * 1000));
        myCircle = mMap.addCircle(circleOptions);
    }

    /**
     * Helper method that passes through the list of bird sightings to be drawn and plots them on
     * the map as necessary.
     * Instances of these markers are added to the markerTags HashMap to allow for determination of
     * which type of marker is being clicked, and display behaviour accordingly.
     */
    public void drawResultList() {
        if (sightingResultList.size() > 0) {
            for (int i = 0; i < sightingResultList.size(); i++) {
                Marker m = mMap.addMarker(sightingResultList.get(i));
                markerTags.put(m, "bird");
            }
        }
    }

    /**
     * Helper method that passes through the list of hotspots to be drawn and plots them on the map
     * as necessary.
     * Instances of these markers are added to the markerTags HashMap to allow for determination of
     * which type of marker is being clicked, and display behaviour accordingly.
     */
    public void drawHotspotList() {
        if (hotspotMarkers.size() > 0) {
            for (int i = 0; i < hotspotMarkers.size(); i++) {
                Marker m = mMap.addMarker(hotspotMarkers.get(i));
                markerTags.put(m, "hotspot");
            }
        }
    }

    @SuppressLint("NewApi")
    protected void onResume() {
        super.onResume();
        // check every second or 100 meters (tweak this for driving/walking etc)
        locationManager.requestLocationUpdates(provider, 10000, 750, this);
        resetMyLocation();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.searchSpecies = "";
    }

    // LOCATION LISTENER
    @Override
    public void onLocationChanged(Location location) {
        lat = (float) (location.getLatitude());
        lon = (float) (location.getLongitude());

        // if follow is enabled, make marker reset to current location before redrawing
        if (followToggle.isChecked()) {
            this.myLatLng = new LatLng(lat, lon);
            resetMyLocation();
        }
        getBirdsNearMe();
        getHotspotsNearMe();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
