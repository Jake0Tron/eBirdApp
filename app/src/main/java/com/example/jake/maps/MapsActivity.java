package com.example.jake.maps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Fragment fragment;

    private Marker myMark;
    private MarkerOptions myMarker;

    private Circle myCirc;
    private CircleOptions circle;

    private LocationManager locationManager;
    private String provider;

    private LatLng myLocation;
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

    // Alert Dialog data fields

    EditText birdBreed,
    birdQuantity,
    birdAge,
    birdNotes;

    Spinner birdBreedingSpinner;
    String birdBreedString, birdQuantityString, birdAgeString, birdNoteString, breedingStatString;

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
        this.maxZoom = 30;  // close
        this.defZoom = 16;
        this.minZoom = 12;  // far

        this.radiusValueView = (TextView) findViewById(R.id.radiusValue);
        this.radiusValue = 1000.0f;
        radiusValueView.setText(radiusValue + " m");

        this.radiusBar = (SeekBar)findViewById(R.id.radiusBar);
        this.radiusBar.setMax(5000);
        this.radiusBar.setProgress((int) radiusValue);

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
                    if (myCirc != null) {
                        myCirc.remove();
                        myCirc = mMap.addCircle(circle);
                    }

                    myMark.remove();
                    myMark = mMap.addMarker(myMarker);

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


        myMarker = new MarkerOptions()
                .position(myLocation)
                .title("My Location: " + lat + " + " + lon);
        myMark = mMap.addMarker(myMarker);

        this.myLocation = new LatLng(lat, lon);
        //Circle RADIUS
        //handle radius changes
        this.circle = new CircleOptions()
                .center(myLocation)
                .strokeWidth(1.5f)
                .radius(radiusValue);
        myCirc = mMap.addCircle(circle);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)      // Sets the center of the map to Mountain View
                .zoom(16)                   // Sets the zoom
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
                myMarker = new MarkerOptions()
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
                    myMark.remove();
                    myMark = mMap.addMarker(myMarker);

                }
                else if (cameraPosition.zoom <= minZoom){
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to me
                            .zoom(minZoom + 0.01f)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
                    myMark.remove();
                    myMark = mMap.addMarker(myMarker);
                }
                else{
                    // lock camera on user
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(curZoomVal)                   // Sets the zoom
                            .bearing(curBearing)                // Sets the orientation of the camera to east
                            .tilt(curTilt)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    myMark.remove();
                    myMark = mMap.addMarker(myMarker);
                }

            }// end onCameraChange
        });// end onCameraChangeListener


        // Handle Long Press on Map to drop a Marker for Sighted Bird
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng longPressPos) {
                final LatLng latLongPress = longPressPos;

                Log.d("LongPress", "Building Alert Dialog");
                // build Alert Dialog
                AlertDialog.Builder adb = new AlertDialog.Builder(currentContext);
                LayoutInflater inflater = MapsActivity.this.getLayoutInflater();

                // TODO: /strings this
                adb
                        .setView(inflater.inflate(R.layout.alert_dialog_add_bird_data, null))
                        .setTitle("Bird Sighting Info")
                                //.setMessage("Enter Data here...")
                                //  set custom view
                        .setPositiveButton("Record", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog d = (Dialog) dialog;
                                Log.d("LongPress", "Button Clicked...");
                                birdBreed = (EditText) d.findViewById(R.id.birdName);
                                birdAge = (EditText) d.findViewById(R.id.birdAge);
                                birdQuantity = (EditText) d.findViewById(R.id.birdQuantity);
                                birdNotes = (EditText) d.findViewById(R.id.birdNotes);
                                Log.d("LongPress", "Spinner Build");
                                birdBreedingSpinner = (Spinner) d.findViewById(R.id.birdBreeding);

                                // array adapter for spinner
                                ArrayAdapter<CharSequence> breedingStatusAdapter =
                                        ArrayAdapter.createFromResource(currentContext,
                                                R.array.breeding_status,
                                                android.R.layout.simple_spinner_item);

                                // set arrayAdapter
                                birdBreedingSpinner.setAdapter(breedingStatusAdapter);

                                birdBreedingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        breedingStatString = parent.getItemAtPosition(position).toString();
                                        Log.d("LongPress","FUCK Sel+");
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        breedingStatString ="None";
                                        Log.d("LongPress","FUCK NOSel");
                                    }
                                });
                                onLongPressAddBird(latLongPress, birdBreed, birdAge, birdQuantity, birdNotes, breedingStatString);

                            }// end on Submit button click
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

    private void onLongPressAddBird(LatLng press, EditText breed, EditText age, EditText quan, EditText notes, String breeding){
        // species

        String breedS = breed.getText().toString();

        // age      ** CHECK THIS FOR VALUES
        int ageI = Integer.valueOf(age.getText().toString());

        // count    ** CHECK THIS FOR VALUES
        int quantityI = Integer.valueOf(quan.getText().toString());

        // details
        String notesS = notes.getText().toString();

        // breeding status

        String birdInfo = String.valueOf(quantityI) + " " + breedS;
        String birdSnip = "Seen at" + press.latitude + " " + press.longitude;

        //add list of Strings for bird info
        birdPositions.add(birdInfo);

        Log.d("BIRD INFO", birdInfo);

        // HANDLE BUTTON PRESS FOR SUBMIT
        mMap.addMarker(new MarkerOptions()
                        .position(press)
                        .alpha(0.7f)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(birdInfo)
                        .snippet(birdSnip)
        );// end marker create
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
