package com.example.jake.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Fragment fragment;
    //private TextView latituteField;
    //private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;
    private LatLng myLocation;

    private CircleOptions circle;
    private float lat, lon;

    private int maxZoom, minZoom, defZoom, tiltValue;

    private float radiusValue;
    private TextView radiusValueView;

    private SeekBar radiusBar;

    // idle camera position
    private CameraPosition campos;

    // list of bird positions
    ArrayList<String> birdPositions;

    Context currentContext;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.currentContext = this;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location = locationManager.getLastKnownLocation(provider);

        this.birdPositions = new ArrayList<>();

        // camera zoom values
        this.maxZoom = 25;
        this.defZoom = 20;
        this.minZoom = 10;

        this.radiusValueView = (TextView) findViewById(R.id.radiusValue);
        this.radiusValue = 1000.0f;
        radiusValueView.setText(radiusValue + " m");

        this.radiusBar = (SeekBar)findViewById(R.id.radiusBar);
        this.radiusBar.setProgress((int) radiusValue);
        this.radiusBar.setMax(5000);

        this.radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // handle seekBar changes
                if (fromUser) {
                    radiusValue = progress;
                    String radV = String.valueOf(progress);
                    radiusValueView.setText(radV + " m");
                    circle = new CircleOptions()
                            .center(myLocation)
                            .strokeWidth(1.5f)
                            .radius(radiusValue);
                    //.fillColor(Color.argb(100,0,180,220))
                    //
                    //.strokeColor(Color.argb(180,0,140,200));
                    mMap.clear();
                    mMap.addCircle(circle);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        }); // end progress bar


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

        this.myLocation = new LatLng(lat, lon);
        // RADIUS
        //handle radius changes
        this.circle = new CircleOptions()
                .center(myLocation)
                .strokeWidth(1.5f)
                .radius(radiusValue);
        mMap.addCircle(circle);

        final MarkerOptions myMarker = new MarkerOptions().position(myLocation).title("My Location: " + lat + " + " + lon);
        mMap.addMarker(myMarker);

        //http://stackoverflow.com/questions/14074129/google-maps-v2-set-both-my-location-and-zoom-in
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)      // Sets the center of the map to Mountain View
                .zoom(defZoom)                   // Sets the zoom
                .tilt(tiltValue)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//       circle = new CircleOptions()
//                            .center(myLocation)
//                            .strokeWidth(1.5f)
//                            .radius(radiusValue);
//        /*
//                //.fillColor(Color.argb(100,0,180,220))
//                .strokeWidth(5f)
//                .strokeColor(Color.argb(180,0,140,200));
//        */
//        mMap.addCircle(circle);

        // handle camera change
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                campos = cameraPosition;
                float curZoomVal = cameraPosition.zoom;
                float curBearing = cameraPosition.bearing;
                float curTilt = cameraPosition.tilt;
                //mMap.clear();
                // Camera Limits
                if (cameraPosition.zoom >= maxZoom){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to my location
                            .zoom(maxZoom - 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to tilt value
                            .build();                   // Creates a CameraPosition from the builder

                }
                else if (cameraPosition.zoom <= minZoom){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(minZoom + 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder

                }
                else{
                    // lock camera on user
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(curZoomVal)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder

                }

            }// end onCameraChange
        });// end onCameraChangeListener

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng longPressPos) {

                final LatLng latLongPress = longPressPos;
                // TODO: set final vars from user input for adb show

                // species
                final String species = "";
                // count
                final int count = 0;
                // details
                final String details = "";

                // TODO: add user input for bird info in view, and retrieve it
                AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                adb.setTitle("Bird Info")
                        .setMessage("Enter Data here...")
                        .setCancelable(false)
                        .setPositiveButton("Record", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // HANDLE BUTTON PRESS FOR SUBMIT


                                String birdInfo = "Bird Location:"
                                        + latLongPress.latitude + " " + latLongPress.longitude
                                        + "\nSpecies: " + species
                                        + "\nNumber seen: " + String.valueOf(count)
                                        + "\nDetails: " + details;

                                //add list of Strings for bird info
                                birdPositions.add(birdInfo);

                                Log.d("BIRD INFO", birdInfo);



                                mMap.addMarker(new MarkerOptions()
                                                .position(latLongPress)
                                                .snippet("SNIP")
                                                .alpha(0.7f)
                                                .draggable(true)
                                                .icon(BitmapDescriptorFactory
                                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                                .title(birdInfo)
                                );
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cancel
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = adb.create();

                dialog.show();
            }
        });

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

    // LOCATION LISTRNER

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
