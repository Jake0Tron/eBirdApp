package com.example.jake.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

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
@SuppressWarnings("ALL")
public class SightingsNearMeActivity
        extends FragmentActivity
        implements AsyncResponse, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private String provider;
    private LatLng myLocation;
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
    NumberPicker radiusPicker;
    // days prior
    NumberPicker daysPriorPicker;


    private String TAG = "eBirdSightings";

    // URL Builder
    URLBuilder uBuilder;
    // http request
    HttpAsyncTask http;
    // list of results from request
    ArrayList<MarkerOptions> resultList;

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

        Location location = locationManager.getLastKnownLocation(provider);

        // camera zoom values
        this.maxZoom = 30;  // close
        this.defZoom = 12;
        this.minZoom = 9;  // far
        this.radiusValue = 5;
        this.daysPriorValue = 4;
        // get number pickers
        daysPriorPicker = (NumberPicker) findViewById(R.id.daysPriorPicker);
        daysPriorPicker.setMinValue(1);
        daysPriorPicker.setMaxValue(30);
        daysPriorPicker.setValue(daysPriorValue);
        daysPriorPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                daysPriorValue = newVal;
            }
        });

        radiusPicker = (NumberPicker) findViewById(R.id.radiusPicker);
        radiusPicker.setMinValue(1);
        radiusPicker.setMaxValue(30);
        radiusPicker.setValue(radiusValue);
        radiusPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                radiusValue = newVal;
                //handle radius changes
                myCircle.remove();

                circleOptions = new CircleOptions()
                        .center(myLocation)
                        .strokeWidth(1.5f)
                        .radius((radiusValue * 1000));
                myCircle = mMap.addCircle(circleOptions);
            }
        });

        this.http = new HttpAsyncTask();
        this.http.delegate = this;
        this.uBuilder = new URLBuilder();

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        myMarkerOptions = new MarkerOptions()
                .position(myLocation)
                .title("My Location: " + lat + " + " + lon);
        myMarker = mMap.addMarker(myMarkerOptions);

        this.myLocation = new LatLng(lat, lon);
        //Circle RADIUS
        //handle radius changes
        this.circleOptions = new CircleOptions()
                .center(myLocation)
                .strokeWidth(1.5f)
                .radius((radiusValue * 1000));
        myCircle = mMap.addCircle(circleOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)      // Sets the center of the map to Mountain View
                .zoom(defZoom)                   // Sets the zoom
                .tilt(tiltValue)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // handle camera change
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                campos = cameraPosition;
                float curZoomVal = cameraPosition.zoom;
                float curBearing = cameraPosition.bearing;
                float curTilt = cameraPosition.tilt;
                myMarkerOptions = new MarkerOptions()
                        .position(myLocation)
                        .title("My Location: " + lat + " + " + lon);
                // Camera Limits
                if (cameraPosition.zoom >= maxZoom){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to my location
                            .zoom(maxZoom - 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to tilt value
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    myMarker.remove();
                    myMarker = mMap.addMarker(myMarkerOptions);

                }
                else if (cameraPosition.zoom <= minZoom){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to me
                            .zoom(minZoom + 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    myMarker.remove();
                    myMarker = mMap.addMarker(myMarkerOptions);
                }
                else{
                    // lock camera on user
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(curZoomVal)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    myMarker.remove();
                    myMarker = mMap.addMarker(myMarkerOptions);
                }

            }// end onCameraChange
        });// end onCameraChangeListener
    }

    // OnSubmit
    public void sightingsNearMeSubmit(View v){
        // build URL for data
        String url = uBuilder.getNearbySightingsURL(myLocation, radiusValue, daysPriorValue);
        Log.d(TAG, radiusValue + " " + daysPriorValue + " " + myLocation);
        //submit http request
        this.http = new HttpAsyncTask();
        this.http.setDelegate(this);
        this.http.execute(url);

    }

    // when data is returned handle it here
    @Override
    public void processFinish(JSONArray result){
        //Log.d(TAG, result.toString());

        // remove previous markers
        mMap.clear();

        // re-add my position
        myMarkerOptions = new MarkerOptions()
                .position(myLocation)
                .title("My Location: " + lat + " + " + lon);
        myMarker = mMap.addMarker(myMarkerOptions);
        // re add radius
        this.circleOptions = new CircleOptions()
                .center(myLocation)
                .strokeWidth(1.5f)
                .radius((radiusValue * 1000));
        myCircle = mMap.addCircle(circleOptions);

        resultList = new ArrayList<>();

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
        try {
            for (int i=0; i < result.length(); i++ ) {
                JSONObject sightingJSON = result.getJSONObject(i);

                // create a markeroptions to hold information about bird sighting
                double birdLat = sightingJSON.getDouble("lat");
                double birdLong = sightingJSON.getDouble("lng");
                String birdComName = sightingJSON.getString("comName");
                String birdSciName = sightingJSON.getString("sciName");
                String locationName = sightingJSON.getString("locName");
                String dateSeen = sightingJSON.getString("obsDt");
                int birdCount = sightingJSON.getInt("howMany");

                Log.d("JSONreturn" , birdComName);

                String markTitle = birdCount + " " + birdComName + " seen at " + locationName;
                String markSnip = birdSciName + " > " + dateSeen + " ";

                LatLng birdPos = new LatLng(birdLat, birdLong);

                MarkerOptions birdMarker = new MarkerOptions();
                birdMarker.draggable(false).
                        icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .alpha(0.7f)
                        .title(markTitle)
                        .snippet(markSnip)
                        .position(birdPos);
                // add to list
                resultList.add(birdMarker);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // if no errors occured
        if (resultList.size() > 0){
            Log.d("ADDING MARKERS", "SIZE > 0");
            for (int i=0; i<resultList.size(); i++) {
                // drop markers on the map
                mMap.addMarker(resultList.get(i));
            }
        }
    }

    @SuppressLint("NewApi")
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 800, 1, this);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    // LOCATION LISTENER

    @Override
    public void onLocationChanged(Location location) {
        lat = (float) (location.getLatitude());
        lon = (float) (location.getLongitude());
        this.myLocation = new LatLng(lat, lon);
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
