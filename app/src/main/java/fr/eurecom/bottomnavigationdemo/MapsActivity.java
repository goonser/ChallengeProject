package fr.eurecom.bottomnavigationdemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import fr.eurecom.bottomnavigationdemo.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GeoFire geoFire;
    GeoQuery geoQuery;

    private Button visibilityButton;
    private boolean firstTime = true;

    // Connect to user button
    private Button connectButton;
    private boolean showConnect = false;
    private String connectToUser = "";


    // Receive TODO
    private TextView connectedText;
    boolean listViewVisible = false;
    private Button toggleRequestsButton;
    private ListView listView;


    //GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 1.0);

    protected LocationManager locationManager = null;
    private String provider;
    Location location;
    public static final int MY_PERMISSIONS_LOCATION = 0;

    HashMap<String, GeoLocation> usersArray = new HashMap<>();


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location locationChanged) {

            if(!(locationChanged.getLatitude() == location.getLatitude() && locationChanged.getLongitude() == location.getLongitude())) {
                location = getLocation();
                geoFire.setLocation(user.getUID(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                user.setLocation(location);
                geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 1.0);
                usersArray.clear();
                map.clear();
                createGeoQuery();
                updateLocationUI();
            }

            if(firstTime) {

                location = getLocation();
                geoFire.setLocation(user.getUID(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                user.setLocation(location);
                geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 1.0);
                usersArray.clear();
                map.clear();
                createGeoQuery();
                updateLocationUI();
                firstTime = false;
            }

        }
    };

    private ActivityMapsBinding binding;
    private GoogleMap map;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final User user = User.getInstance();




    private Location getLocation() {
        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission: ", "To be checked");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_LOCATION);

        } else
            Log.i("Permission: ", "GRANTED");
        location = locationManager.getLastKnownLocation(provider);
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.mLocationListener);

        return location;
    }

    //@Override
    public void onLocationChanged(Location location) {
        Log.i("Location","LOCATION CHANGED!!!"); //updateLocationView();
    }
    //@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    // @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }
    //@Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checking if user credentials are set
        //if they are not set, start activity to set credentials
        //TODO

        try {
            binding = ActivityMapsBinding.inflate(getLayoutInflater());
        } catch (Exception e) {
            Log.e("WTF", "onCreate", e);
            throw e;
        }
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), Integer.toString(R.string.google_maps_key));

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.map);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.shop:
                        startActivity(new Intent(getApplicationContext(), ShopActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.map:
                        return true;
                }
                return false;
            }
        });

        location = getLocation();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Locations");
        geoFire = new GeoFire(ref);

        //geoFire.setLocation("UserDemoStj", new GeoLocation(63.4684, 10.9172));
        //geoFire.setLocation("UserDemoFarAwayTrd", new GeoLocation(63.4250, 10.4428));
        visibilityButton = findViewById(R.id.visibilityButton);
        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility();
            }
        });

        // Create connectButton
        connectButton = findViewById(R.id.connectButton);
        connectButton.setVisibility(View.INVISIBLE);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectUser();
            }
        });

        // Received text field
        connectedText = findViewById(R.id.connectedText);
        connectedText.setVisibility(View.INVISIBLE);
        connectedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeConnectedText();
            }
        });

        // Toggle listView requests button
        toggleRequestsButton = findViewById(R.id.toggleRequestsButton);
        toggleRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRequestsView();
            }
        });

        // Create pendingRequests list view
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("ConnectionRequest");

        final ArrayList<ConnectionRequest> requestsArray = new ArrayList<>();
        final RequestAdapter adapter = new RequestAdapter(this, requestsArray);

        listView = findViewById(R.id.pendingRequests);
        listView.setAdapter(adapter);
        listView.setBackgroundColor(Color.WHITE);
        listView.setVisibility(View.INVISIBLE);

        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("JAN", "DATA CHANGED");


                requestsArray.clear();
                adapter.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    ConnectionRequest read_request = dataSnapshot.getValue(ConnectionRequest.class);
                    //read_request.setMessageUser(dataSnapshot.getKey());
                    String[] message = read_request.getMessageText().split(" ");
                    String type = message[0];
                    Log.d("JAN", "type:!"+type+"!");

                    String recipient = "";
                    for (int i =1; i< message.length; i++) {
                        recipient += " " + message[i];
                    }
                    recipient = recipient.trim();
                    Log.d("JAN", "recipient:!"+recipient+"!");
                    Log.d("JAN", "myusername:!"+user.getName()+"!");


                    String fromUser = read_request.getMessageUser();
                    if (recipient.equalsIgnoreCase(user.getName())) {
                        Log.d("JAN", "GOT REQUEST1");

                        if (type.equalsIgnoreCase("REQUEST")) {
                            Log.d("JAN", "GOT REQUEST2");
                            requestsArray.add(read_request);
                            adapter.notifyDataSetChanged();
                        } else {
                            // CONFIRMATION
                            // Send confirmation back
                            Log.d("JAN", "GOT CONFIRMATION");

                            connectedText.setVisibility(View.VISIBLE);
                            connectedText.setText(fromUser + " has accepted you request!");
                        }
                    }

                    long currentTime = new Date().getTime();
                    long timeBeforeRequestDeletion = 1000*60*10; // 10 min
                    if (currentTime - read_request.getMessageTime() > timeBeforeRequestDeletion) {
                        dataSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("REQUEST", "loadRequest:onCancelled", error.toException());

            }
        });

        // Accept connection request when clicking on listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user = requestsArray.get(position).getMessageUser();
                Log.d("JAN", user);
                FirebaseDatabase.getInstance()
                        .getReference("ConnectionRequest")
                        .push()
                        .setValue(new ConnectionRequest("CONFIRMATION " + user,
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );
            }
        });

        createGeoQuery();

        Log.i("onCreate", "at end");

    }

    private void createGeoQuery() {

        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 1.0);

        final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation geoLocation) {
                //System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, geoLocation.latitude, geoLocation.longitude));
                usersArray.put(key, geoLocation);
                for(String useKey : usersArray.keySet()) {
                    Log.i("UserKey: ", useKey+ " location: " +usersArray.get(useKey).toString());
                }

                updateLocationUI();
            }

            @Override
            public void onKeyExited(String key) {
                //System.out.println(String.format("Key %s is no longer in the search area", key));
                usersArray.remove(key);
                for(String useKey : usersArray.keySet()) {
                    Log.i("UserKey: ", useKey+ " location: " +usersArray.get(useKey).toString());
                }
                updateLocationUI();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation geoLocation) {
                //System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, geoLocation.latitude, geoLocation.longitude));
                usersArray.remove(key);
                usersArray.put(key, geoLocation);

                for(String useKey : usersArray.keySet()) {
                    Log.i("Moved! in array: ", useKey+ "location: " +usersArray.get(useKey).toString());
                }
                updateLocationUI();
            }

            @Override
            public void onGeoQueryReady() {
                Log.i("onGeoQueryReady", "ready");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.i("onGeoQueryError", error.toString());

            }

        };

        geoQuery.addGeoQueryEventListener(geoQueryEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(this.mLocationListener);
    }


    /**
     * Saves the state of the map when the activity is paused.
     */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
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
    public void onMapReady(GoogleMap map) {
        this.map = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {


                return null;
            }
        });



        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //createGeoQuery(geoFire, location);

        // Add InfoWindowClick to map
        map.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    // This shows the connectButton when marker is tapped
                    @Override
                    public void onInfoWindowClick(@NonNull Marker marker) {
                        Log.d("JAN", "CLICKED MARKER1");

                        showConnect = true;
                        connectButton.setVisibility(View.VISIBLE);
                        connectButton.setText("Connect to " + marker.getTitle() + "!");
                        connectToUser = marker.getTitle();
                        Log.d("JAN", "CLICKED MARKER");
                    }
                }
        );

        map.setOnInfoWindowCloseListener(
                new GoogleMap.OnInfoWindowCloseListener() {
                    // This hides the connectButton when marker is closed
                    @Override
                    public void onInfoWindowClose(@NonNull Marker marker) {
                        showConnect = false;
                        connectButton.setVisibility(View.INVISIBLE);
                        connectToUser = "";
                        Log.d("JAN", "CLOSED MARKER");

                    }
                }
        );

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();

                            if (lastKnownLocation != null) {
                                Log.i("TAG", "lastKnownLocation not null: "+lastKnownLocation);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d("TAG", "Current location is null. Using defaults.");
                            Log.e("TAG", "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
            //System.out.println("lastknownLoCATION: "+lastKnownLocation);
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

        //System.out.println("lastKnownLocation: "+lastKnownLocation);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        map.clear();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl("gs://challengeproject-334921.appspot.com/Avatars/Avatar1.png");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");


        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {


                    try {
                        for (String keys : usersArray.keySet()) {
                            if (user.getUID() != keys) {
                                //testing new marker:
                                final long ONE_MEGABYTE = 1024 * 1024;
                                DataSnapshot s = task.getResult();
                                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        //imageView.setImageBitmap(bmp);

                                        BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bmp);
                                        DataSnapshot snapshot = task.getResult();
                                        String snippet = (String) snapshot.child(keys).child("status").getValue();


                                        GeoLocation usLoc = usersArray.get(keys);

                                        if(usLoc == null) {
                                            return;
                                        }
                                        double lng = usLoc.longitude;
                                        double lat = usLoc.latitude;
                                        LatLng latLng = new LatLng(lat, lng);
                                        map.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title((String) snapshot.child(keys).child("name").getValue())
                                                .icon(bd)
                                                .snippet(snippet));
                                        //Log.i("Marker: ", ""+(String) snapshot.child(keys).child("name").getValue());
                                        //map.addMarker(new MarkerOptions().position(latLng).title((String) snapshot.child(keys).child("name").getValue()).snippet("HEEEI"));
                                        // map.addMarker(marker);


                                        Log.i("Image", "success");

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.i("Image", "Error setting image");
                                        GeoLocation usLoc = usersArray.get(keys);
                                        double lng = usLoc.longitude;
                                        double lat = usLoc.latitude;
                                        LatLng latLng = new LatLng(lat, lng);
                                        map.addMarker(new MarkerOptions().position(latLng).title(keys));
                                    }
                                });

                            }
                        }
                    }
                    catch (Exception e) {
                        Log.e("Marker: ", "exception setting marker: " + e.toString());
                    }
                }

            }
        });

        if (locationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);


        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            lastKnownLocation = null;
            getLocationPermission();
        }
    }

    private void toggleVisibility(){
        if (user.isVisible()){
            user.setVisible(false);
            user.setLocation(location);
            Toast.makeText(getApplicationContext(),"You are no longer visible to other users",Toast.LENGTH_SHORT).show();
            visibilityButton.setText("Invisible");
        }
        else{
            user.setVisible(true);
            user.setLocation(location);
            Toast.makeText(getApplicationContext(),"You are now visible to other users",Toast.LENGTH_SHORT).show();
            visibilityButton.setText("Visible");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("STOP", "onStop called");
    }


    // Connect to user method runs once button has been clicked.
    private void connectUser() {

        // Read the input field and push a new instance
        // of ConnectionRequest to the Firebase database
        FirebaseDatabase.getInstance()
                .getReference("ConnectionRequest")
                .push()
                .setValue(new ConnectionRequest("REQUEST " + this.connectToUser,
                        FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getDisplayName())
                );
    }

    private void closeConnectedText() {
        connectedText.setVisibility(View.INVISIBLE);
    }

    private void toggleRequestsView() {
        if (listViewVisible) {
            listView.setVisibility(View.INVISIBLE);
            listViewVisible = false;
        } else {
            listView.setVisibility(View.VISIBLE);
            listViewVisible = true;
        }
    }


}


