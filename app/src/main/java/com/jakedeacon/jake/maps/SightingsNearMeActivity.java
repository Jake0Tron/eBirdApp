package com.jakedeacon.jake.maps;

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
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
    // list of markers created after JSON received
    ArrayList<MarkerOptions> matchingMarkers;
    // list of titles from JSON
    ArrayList<String> matchingBirdTitles;
    // list of subtitles from JSON
    ArrayList<String> matchingBirdSubTitles;

    // alert dialog for multiple birds
    AlertDialog multiBirdAlert;
    // Spinner to handle multiple birds in the alertdialog
    Spinner multiBirdSpinner;

    // Follow Toggle
    ToggleButton followToggle;

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

        this.followToggle = (ToggleButton) findViewById(R.id.toggleFollow);
        this.followToggle.setChecked(true);

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
            Toast.makeText(this, "Please Enable GPS!", Toast.LENGTH_LONG).show();
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

                matchingBirdTitles.clear();
                matchingBirdSubTitles.clear();

                for (int i = 0; i < resultList.size(); i++) {

                    double markerLat = resultList.get(i).getPosition().latitude;
                    double markerLon = resultList.get(i).getPosition().longitude;

                    double clickLat = marker.getPosition().latitude;
                    double clickLon = marker.getPosition().longitude;

                    String matchingBirdDataTitle = resultList.get(i).getTitle();
                    String matchingBirdDataSubTitle = resultList.get(i).getSnippet();

                    if ((markerLat == clickLat) && (markerLon == clickLon)) {
                        matchingBirdTitles.add(matchingBirdDataTitle);
                        matchingBirdSubTitles.add(matchingBirdDataSubTitle);
                    }
                }

                if (matchingBirdTitles.size() > 0) {
                    LayoutInflater inflater = SightingsNearMeActivity.this.getLayoutInflater();
                    AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                    String titleString = getResources().getString(R.string.near_me_alert_title) + " " +
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
            }
        });// end markerclickListener

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng touchLatLng) {
                // only allow long press if Follow is disabled
                if (!followToggle.isChecked()) {
                    myLatLng = touchLatLng;
                    getBirdsNearMe();
                    drawMyLocation();
                } else {
                    // pop toast to alert user that toggle is on
                    Toast.makeText(currentContext, R.string.near_me_toggle_error, Toast.LENGTH_SHORT)
                            .show();
                }
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
        resetMyLocation();
        resetCameraLocation();
    }

    public void resetMyLocation() {
        this.myLatLng = new LatLng(lat, lon);
        myLocation = locationManager.getLastKnownLocation(provider);
        getBirdsNearMe();
    }

    public void resetCameraLocation() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLatLng)
                .zoom(defZoom)
                .tilt(tiltValue)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // when data is returned handle it here
    @Override
    public void processFinish(JSONArray result) {
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
        // check every second or 100 meters (tweak this for driving/walking etc)
        locationManager.requestLocationUpdates(provider, 2000, 100, this);
        resetMyLocation();
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

        // if follow is enabled, make marker reset to current location before redrawing
        if (followToggle.isChecked()) {
            this.myLatLng = new LatLng(lat, lon);
            resetMyLocation();
        }
        getBirdsNearMe();
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
