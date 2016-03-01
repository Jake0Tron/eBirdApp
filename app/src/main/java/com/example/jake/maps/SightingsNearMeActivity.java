package com.example.jake.maps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

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
    ArrayList<MarkerOptions> matchingMarkers;

    ArrayList<String> matchingBirdTitles;
    ArrayList<String> matchingBirdSubTitles;

    // alert dialog for multiple birds
    AlertDialog multiBirdAlert;
    // Spinner to handle multiple birds in the alertdialog
    Spinner multiBirdSpinner;

    Context currentContext;

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

        // get number pickers
        daysPriorPicker = (NumberPicker) findViewById(R.id.daysPriorPicker);
        daysPriorPicker.setMinValue(1);
        daysPriorPicker.setMaxValue(30);
        daysPriorPicker.setValue(daysPriorValue);
        daysPriorPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                daysPriorValue = newVal;
                getBirdsNearMe();
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
                drawViewRadius();
                getBirdsNearMe();
            }
        });

        resultList = new ArrayList<MarkerOptions>();
        matchingMarkers = new ArrayList<MarkerOptions>();
        matchingBirdTitles = new ArrayList<String>();
        matchingBirdSubTitles = new ArrayList<String>();

        this.http = new HttpAsyncTask();
        this.http.delegate = this;
        this.uBuilder = new URLBuilder();

        // Initialize the location fields
        if (myLocation != null) {
            onLocationChanged(myLocation);
        } else {
            Log.d(TAG, "NO LOCATION");
            Toast.makeText(this, "Please Enable GPS!", Toast.LENGTH_LONG).show();
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

        this.myLatLng = new LatLng(lat, lon);
        drawMyLocation();
        //drawViewRadius();

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
                campos = cameraPosition;
                float curZoomVal = cameraPosition.zoom;
                float curBearing = cameraPosition.bearing;
                float curTilt = cameraPosition.tilt;

                drawMyLocation();

                // Camera Limits
                if (cameraPosition.zoom >= maxZoom) {
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLatLng)      // Sets the center of the map to my location
                            .zoom(maxZoom - 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to tilt value
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    //myMarker.remove();
                    //myMarker = mMap.addMarker(myMarkerOptions);

                } else if (cameraPosition.zoom <= minZoom) {
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLatLng)      // Sets the center of the map to me
                            .zoom(minZoom + 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    // myMarker.remove();
                    //myMarker = mMap.addMarker(myMarkerOptions);
                } else {
                    // lock camera on user
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLatLng)      // Sets the center of the map to Mountain View
                            .zoom(curZoomVal)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    //myMarker.remove();
                    //myMarker = mMap.addMarker(myMarkerOptions);
                }

            }// end onCameraChange
        });// end onCameraChangeListener

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Log.d(TAG, "MARKER SELECT : " + matchingMarkers.size());

                //if (!marker.equals(myMarker)) {
                // Create list of data of all birds at this marker location

                matchingBirdTitles.clear();
                matchingBirdSubTitles.clear();

                for (int i = 0; i < resultList.size(); i++) {

                    double markerLat = resultList.get(i).getPosition().latitude;
                    double markerLon = resultList.get(i).getPosition().longitude;

                    double clickLat = marker.getPosition().latitude;
                    double clickLon = marker.getPosition().longitude;

                    String matchingBirdDataTitle = resultList.get(i).getTitle();
                    String matchingBirdDataSubTitle = resultList.get(i).getSnippet();

                    Log.d(TAG, matchingBirdDataTitle + " " + matchingBirdDataSubTitle);
                    Log.d(TAG, markerLat + " " + markerLon);

                    if ((markerLat == clickLat) && (markerLon == clickLon)) {
                        //Log.d(TAG, "MATCH!");
                        matchingBirdTitles.add(matchingBirdDataTitle);
                        matchingBirdSubTitles.add(matchingBirdDataSubTitle);
                    } else {
                        //Log.d(TAG, "NO MATCH!");
                    }
                }

                if (matchingBirdTitles.size() > 0) {
                    Log.d(TAG, "Multiple birds at marker");

                    // add alertdialog with spinner to handle multiple birds
                    LayoutInflater inflater = SightingsNearMeActivity.this.getLayoutInflater();
                    AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                    String titleString = getResources().getString(R.string.near_me_alert_title) + " " +
                            String.valueOf(matchingBirdTitles.size());

                    adb.setView(inflater.inflate(R.layout.alert_dialog_multiple_sightings_near_me, null))
                            .setTitle(titleString)
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {    //TODO: Strings this
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //matchingBirdTitles = new ArrayList<String>();
                                    dialog.cancel();
                                }
                            });

                    // build AlertDialog
                    AlertDialog dialog = adb.create();
                    dialog.show();

                    // set spinner view
                    multiBirdSpinner = (Spinner) dialog.findViewById(R.id.multi_bird_spinner);

                    if (multiBirdSpinner != null) {
                        TextSubTextAdapter adapter
                                = new TextSubTextAdapter(SightingsNearMeActivity.this,
                                matchingBirdTitles,
                                matchingBirdSubTitles);
                        multiBirdSpinner.setAdapter(adapter);
                        multiBirdSpinner.setSelection(0, true);
                        multiBirdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
                return true;
            }
        });// end markerclickListener

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // TODO: set myLocation marker to longClick Location and zoom into position selected

                myLatLng = latLng;
                getBirdsNearMe();
                drawMyLocation();
            }
        });// end mapLongClickListener

        getBirdsNearMe();
    }

    public void getBirdsNearMe() {
        String url = uBuilder.getNearbySightingsURL(myLatLng, radiusValue, daysPriorValue);
        this.http = new HttpAsyncTask();
        this.http.setDelegate(this);
        this.http.execute(url);
    }


    public void resetLocation(View v) {
        //TODO: fix this up

        this.myLatLng = new LatLng(lat, lon);
        myLocation = locationManager.getLastKnownLocation(provider);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLatLng)
                .zoom(defZoom)
                .tilt(tiltValue)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        getBirdsNearMe();

    }

    // when data is returned handle it here
    @Override
    public void processFinish(JSONArray result) {
        Log.d(TAG, result.toString());

        // There is something odd with how eBird filters their results via location and distance from it.
        // after certain distances birds will be removed from the list for reasons I can't tell...
        // Will have to look into this further.

        resultList.clear();
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
                //Log.d(TAG, sightingJSON.toString());
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
                    Log.d(TAG, "BirdLat is null");
                    birdLat = 0.0;
                    e.printStackTrace();
                }

                try {
                    birdLong = sightingJSON.getDouble("lng");
                } catch (JSONException e) {
                    Log.d(TAG, "BirdLng is null");
                    birdLong = 0.0;
                    e.printStackTrace();
                }

                try {
                    birdComName = sightingJSON.getString("comName");
                } catch (JSONException e) {
                    Log.d(TAG, " comName is null");
                    birdComName = "";
                    e.printStackTrace();
                }

                try {
                    birdSciName = sightingJSON.getString("sciName");
                } catch (JSONException e) {
                    Log.d(TAG, birdComName + " sciName is null");
                    birdSciName = "";
                    e.printStackTrace();
                }

                try {
                    locationName = sightingJSON.getString("locName");
                } catch (JSONException e) {
                    Log.d(TAG, birdComName + " locName is null");
                    locationName = "";
                    e.printStackTrace();
                }

                try {
                    dateSeen = sightingJSON.getString("obsDt");
                } catch (JSONException e) {
                    Log.d(TAG, birdComName + " ObsDt is null");
                    dateSeen = "";
                    e.printStackTrace();
                }

                try {
                    birdCount = String.valueOf(sightingJSON.getInt("howMany"));
                } catch (JSONException e) {
                    Log.d(TAG, birdComName + " birdCount is null");
                    birdCount = "X";
                    //e.printStackTrace();
                }

                String markTitle = birdCount + " " + birdComName + " seen at " + locationName;
                String markSnip = birdSciName + " > " + dateSeen + " " + birdLat + " " + birdLong;

                LatLng birdPos = new LatLng(birdLat, birdLong);

                MarkerOptions birdMarker = new MarkerOptions();
                birdMarker
                        .draggable(false)
//                        .icon(BitmapDescriptorFactory
//                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .alpha(0.7f)
                        .title(markTitle)
                        .snippet(markSnip)
                        .position(birdPos)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bird_icon_small));

                resultList.add(birdMarker);

                displayCount++;
            }
            Toast.makeText(this, displayCount + " sightings in this area : " + resultList.size(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawMyLocation();
        drawViewRadius();
        drawResultList();
    }

    public void drawMyLocation() {
        // re-add my position
        if (myMarker != null)
            myMarker.remove();
        myMarkerOptions = new MarkerOptions()
                .position(myLatLng)
                .title("My Location: " + lat + " + " + lon)
                        //.flat(true)
                        //.rotation(myLocation.getBearing())
                        //.anchor(0.5f, 0.5f)
                .draggable(false);
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.image_preview));
        myMarker = mMap.addMarker(myMarkerOptions);
    }

    public void drawViewRadius() {
        //Circle RADIUS
        //handle radius changes
        this.circleOptions = new CircleOptions()
                .center(myLatLng)
                .strokeWidth(1.5f)
                .radius((radiusValue * 1000));
        myCircle = mMap.addCircle(circleOptions);
    }

    public void drawResultList() {
        // if no errors occured
        if (resultList.size() > 0) {
            for (int i = 0; i < resultList.size(); i++) {
                // drop markers on the map
                mMap.addMarker(resultList.get(i));
            }
        }
    }

    public void drawFannedMarkerList(ArrayList<MarkerOptions> markerlist) {
        // add remaining markers (fanned)
        for (int i = 0; i < markerlist.size(); i++) {
            markerlist.get(i)
                    .flat(true)
                    .rotation((float) i * (360 / markerlist.size()));
            mMap.addMarker(markerlist.get(i));
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
