package com.example.jake.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // latituteField = (TextView) findViewById(R.id.lat);
        // longitudeField = (TextView) findViewById(R.id.lon);

        // From Vogella
        // http://www.vogella.com/tutorials/AndroidLocationAPI/article.html

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
           // latituteField.setText("Location not available");
           // longitudeField.setText("Location not available");
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

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        this.myLocation = new LatLng(lat, lon);

        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location: " + lat + " + " + lon));
        //http://stackoverflow.com/questions/14074129/google-maps-v2-set-both-my-location-and-zoom-in
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        circle = new CircleOptions()
                .center(myLocation)
                .radius(50.0)
                .fillColor(Color.argb(100,0,180,220))
                .strokeWidth(5f)
                .strokeColor(Color.argb(180,0,140,200));
        mMap.addCircle(circle);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // Camera Limits
                if (cameraPosition.zoom > 18){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(18)                   // Sets the zoom
                                    //.bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    circle = new CircleOptions()
                            .center(myLocation)
                            .radius(50.0)
                            .fillColor(Color.argb(100,0,180,220))
                            .strokeWidth(1.5f)
                            .strokeColor(Color.argb(180,0,140,200));
                    mMap.addCircle(circle);
                    mMap.addCircle(circle);
                }
                if (cameraPosition.zoom < 17){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(17)                   // Sets the zoom
                                    //.bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    circle = new CircleOptions()
                            .center(myLocation)
                            .radius(50.0)
                            .fillColor(Color.argb(100,0,180,220))
                            .strokeWidth(1.5f)
                            .strokeColor(Color.argb(180,0,140,200));
                    mMap.addCircle(circle);
                    mMap.addCircle(circle);
                }
            }
        });

    }


    @SuppressLint("NewApi")
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(provider, 400, 1, this);
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



        //Log.d("LOCATION",lat + " x " + lon);
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
