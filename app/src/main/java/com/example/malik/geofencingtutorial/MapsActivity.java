package com.example.malik.geofencingtutorial;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
//import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, LocationListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private Location lastLocation;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private TextView textLat, textLong;
    private MapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private int REQ_PERMISSION = 1;
Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);
        //initGMaps();
        this.context = getApplicationContext();
        createGoogleApi();
    }
    private void initGMaps(){
        //mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi(LocationServices.API )
                .build();
        }
    }
    @Override
    protected void onStart() {
            super.onStart();
            // Call GoogleApiClient connection when starting the Activity
            googleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }
    @Override
    public void onConnected( Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }
    //Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                    writeLastLocation();
                    startLocationUpdates();
                } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }
    private LocationRequest locationRequest;
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged ["+location+"]");
            lastLocation = location;
            writeActualLocation(location);
        }


    };

    // Start location Updates
            private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
                /*com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "onLocationChanged ["+location+"]");
                        lastLocation = location;
                        writeActualLocation(location);
                    }
                };*/
        locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(UPDATE_INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL);
        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,  this);
    }



    // Write location coordinates on UI
        private void writeActualLocation(Location location) {
        textLat.setText( "Lat: " + location.getLatitude() );
        textLong.setText( "Long: " + location.getLongitude() );
            markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }
        private void writeLastLocation() {
            writeActualLocation(lastLocation);
    }
        // Check for permission to access Location
        private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED );
    }

// Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(this,
            new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQ_PERMISSION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 1: {
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();
                   } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }
// App cannot work without the permissions
        private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        }
    @Override
    public void onMapClick(LatLng latLng) {
    Log.d(TAG, "onMapClick("+latLng +")");
    markerForGeofence(latLng);
    }

    private Marker locationMarker;
    // Create a Location Marker
    private void markerLocation(LatLng latLng) {
    Log.i(TAG, "markerLocation("+latLng+")");
    String title = latLng.latitude + ", " + latLng.longitude;
    MarkerOptions markerOptions = new MarkerOptions()
        .position(latLng)
        .title(title);
    if ( mMap!=null ) {
       // Remove the anterior marker
        if ( locationMarker != null )
        locationMarker.remove();
        locationMarker = mMap.addMarker(markerOptions);
        float zoom = 14f;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);
    }
    }
    private Marker geoFenceMarker;
    // Create a marker for the geofence creation
    private void markerForGeofence(LatLng latLng) {
    Log.i(TAG, "markerForGeofence("+latLng+")");
    String title = latLng.latitude + ", " + latLng.longitude;
    // Define marker options
    MarkerOptions markerOptions = new MarkerOptions()
        .position(latLng)
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        .title(title);
    if ( mMap!=null ) {
        // Remove last geoFenceMarker
    if (geoFenceMarker != null)
            geoFenceMarker.remove();

        geoFenceMarker = mMap.addMarker(markerOptions);
    }
    }
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius ) {
    Log.d(TAG, "createGeofence");
    return new Geofence.Builder()
        .setRequestId(GEOFENCE_REQ_ID)
            .setCircularRegion( latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration( GEO_DURATION )
            .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                | Geofence.GEOFENCE_TRANSITION_EXIT )
            .build();
    }
    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
    Log.d(TAG, "createGeofenceRequest");
    return new GeofencingRequest.Builder()
            .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
            .addGeofence( geofence )
            .build();
    }
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
    Log.d(TAG, "createGeofencePendingIntent");
    if ( geoFencePendingIntent != null )
    return geoFencePendingIntent;

    Intent intent = new Intent( this, GeofenceTransitionService.class);
    return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }
    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
Log.d(TAG, "addGeofence");
if (checkPermission())
LocationServices.GeofencingApi.addGeofences(
               googleApiClient,
               request,       createGeofencePendingIntent()).setResultCallback(MapsActivity.this);
    }
    // Add the created GeofenceRequest to the device's monitoring list
   // private void addGeofence(GeofencingRequest request) {
    //Log.d(TAG, "addGeofence");
    //if (checkPermission()){

    //}
           /* LocationServices.GeofencingApi.addGeofences(googleApiClient,
                request, createGeofencePendingIntent()).setResultCallback(this)
                        new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "SAVE: OK");

                    } else {
                        Log.d(TAG, "not successfull");
                    }

                }
            });*/
    }
    //@Override
    public void onResult(Status status) {
    Log.i(TAG, "onResult: " + status);
    if ( status.isSuccess() ) {
        drawGeofence();
    } else {
        // inform about fail
        Toast.makeText(MapsActivity.this,"Failed",Toast.LENGTH_SHORT).show();
    }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
    Log.d(TAG, "drawGeofence()");
    if ( geoFenceLimits != null )
        geoFenceLimits.remove();
        CircleOptions circleOptions = new CircleOptions()
            .center( geoFenceMarker.getPosition())
            .strokeColor(Color.argb(50, 70,70,70))
            .fillColor( Color.argb(100, 150,150,150) )
            .radius( GEOFENCE_RADIUS );
    geoFenceLimits = mMap.addCircle( circleOptions );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    switch ( item.getItemId() ) {
        case R.id.geofence: {
            startGeofence();
            return true;
        }
    }
    return super.onOptionsItemSelected(item);
    }

    // Start Geofence creation process
    private void startGeofence() {
    Log.i(TAG, "startGeofence()");
    if( geoFenceMarker != null ) {
        Geofence geofence = createGeofence( geoFenceMarker.getPosition(), GEOFENCE_RADIUS );
        GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
        addGeofence( geofenceRequest );
    } else {
        Log.e(TAG, "Geofence marker is null");
    }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }
    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
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
        Log.d(TAG, "onMapReady()");
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);


        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
    // Callback called when Map is touched

    // Callback called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition() );
        return false;
}

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        writeActualLocation(location);
    }
    static Intent makeNotificationIntent(Context geofenceService, String msg)
    {
        Log.d(TAG,msg);
        return new Intent(geofenceService,MapsActivity.class);
    }
}
