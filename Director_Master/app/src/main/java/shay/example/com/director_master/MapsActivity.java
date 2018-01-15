package shay.example.com.director_master;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnCircleClickListener,
        GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback {


    private static final int MINIMUM_RADIUS = 10;
    private final String[] map_style = {"Default", "Silver", "Navy", "Night", "Dark"};
    private LatLng initGeoCircleLocation;
    private LatLng latLng;
    private Circle circle;
    private GoogleMap mMap;
    private boolean onCreateCircle = false;
    private LatLng init_cords;// the initial center screen coordinates for reference on map moved ..
    private int newRadialDistance;
    private Marker radiusMarker;
    private int selected_style = R.raw.map_style;// default map style

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert switch
        assert actionBar != null;
        actionBar.setCustomView(R.layout.actionbar_layout);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

        FloatingActionButton myFab = findViewById(R.id.fab_maps);



        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("onClick", "myFab");
                Intent intentMaps = new Intent(MapsActivity.this, MainActivity.class);

                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_, R.transition.slide_out_).toBundle();
                startActivity(intentMaps, animation);
                MapsActivity.this.finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // map dropdown style selection
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, R.id.weekofday, map_style);

        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(spinnerAdapter);

        RadioGroup rg = findViewById(R.id.radio_group);// two radio buttons

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            //https://stackoverflow.com/users/2695282/syed-raza-mehdi
            public float getZoomLevel() {
                float zoomLevel;
                if (circle != null) {
                    double radius = circle.getRadius();
                    double scale = radius / 250;// I modified this from 500
                    zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
                } else {
                    zoomLevel = 17.5F;
                }
                return zoomLevel;
            }


            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                CameraPosition cameraPosition = null;
                LatLngBounds init_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

                latLng = ((latLng == null) ? init_bounds.getCenter() : latLng);


                if (checkedId == R.id.radio1) {
                    Log.d("chk", "First");
                    //      LatLngBounds bounds =  calculateBounds(latLng,circle.getRadius());
                    //    Log.e("circle.getRadius()",""+circle.getRadius());
                    cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(getZoomLevel())
                            .bearing(270)
                            .tilt(66)
                            .build();
                    //       mMap.setLatLngBoundsForCameraTarget(bounds);

                } else if (checkedId == R.id.radio2) {
                    Log.d("chk", "Second");

                    cameraPosition = new CameraPosition.Builder()
                            .target(latLng)      // Sets the center of the map
                            .zoom(15)                   // Sets the zoom
                            .bearing(-1)                // -90 = west, 90 = east
                            .tilt(0)
                            .build();

                }
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        });


    }

    private void setMapStyle(int style) {
        if (mMap == null) {// this method being called on orient change before map is ready
            return;
        }
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, style));

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //  Reference String[] map_style = { "Default", "Silver", "Navy", "Night", "Dark",  };
        Log.e("id ", "" + map_style[position]);
        String selected = map_style[position];

        switch (selected) {
            case "Silver":
                selected_style = R.raw.silver_style;
                break;
            case "Navy":
                selected_style = R.raw.navy_style;
                break;
            case "Night":
                selected_style = R.raw.night_style;
                break;
            case "Dark":
                selected_style = R.raw.dark_map_style;
                break;
            default:
                selected_style = R.raw.map_style;
                break;
        }
        setMapStyle(selected_style);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {



        mMap = googleMap;

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
        mMap.setBuildingsEnabled(true);
        //  TODO here
        getGeoFenceLocation();// get the initial circle location in firebase and set the camera

        setUsersMarkers();

        mMap.setOnMapLongClickListener(this);// long click to init the circle
        // listener to finished blue circle
        mMap.setOnCircleClickListener(this);
    }

    private void getGeoFenceLocation() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference geocircle = database.getReference("master").child("geocircle");


        geocircle.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot geo_circle) {
                Log.e("DataSnapshot circle", "" + geo_circle.child("lat").getValue());
                double latitude = (double) geo_circle.child("lat").getValue();
                double longitude = (double) geo_circle.child("lon").getValue();
                initGeoCircleLocation = new LatLng(latitude, longitude);
                // move the camera to the initial circle location
                CameraPosition INIT =
                        new CameraPosition.Builder()
                                .target(initGeoCircleLocation)
                                .zoom(17.5F)
                                .bearing(290F) // orientation
                                .tilt(50F) // viewing angle
                                .build();
                // use GoogleMap mMap to move camera into position
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void setUsersMarkers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersNode = database.getReference("users");
        usersNode.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                LatLng newLocation = new LatLng(
                        dataSnapshot.child("location").child("latitude").getValue(Double.class),
                        dataSnapshot.child("location").child("longitude").getValue(Double.class)
                );

                String name = dataSnapshot.child("name").getValue(String.class);

                mMap.addMarker(new MarkerOptions()
                        .position(newLocation)
                        .title(name))
                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon));


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapLongClick(LatLng loc) {

        //  mMap.clear();
        if (circle != null) {
            Log.e("circle != null", "remove");
            circle.remove();// clean up previous object if exists .. old habit from Lua
            circle = null;
            radiusMarker.remove();// remove the pin image
        }
        onCreateCircle = true;
        latLng = loc;

        LatLngBounds init_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //    Log.e("onCameraIdle", "bounds: " + init_bounds.getCenter());
        init_cords = init_bounds.getCenter();

        //   Log.e("Click:", "LAT: " + ev.latitude + " LON:" + ev.longitude);


        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(MINIMUM_RADIUS)
                .strokeWidth(4f)
                .strokeColor(Color.argb(150, 255, 91, 86))
                .fillColor(Color.argb(100, 255, 182, 103)));

        radiusMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Set Geo fence")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
        radiusMarker.showInfoWindow();
    }


    private int calculateDifferentialDistance(LatLng start, LatLng end) {

        float[] results = new float[1];
        double startLatitude = start.latitude;// initial start center of map
        double startLongitude = start.longitude;
        double endLatitude = end.latitude;// new center of map after move
        double endLongitude = end.longitude;
        Location.distanceBetween(startLatitude, startLongitude,
                endLatitude, endLongitude, results);
        int distance = (int) results[0];// cast to clean number
        //  Log.e("Results", distance + "");
        return distance;
    }

 // comment
    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            // Log.e("", "The user gestured on the map.");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            //   Log.e("", "The user tapped something on the map.");

        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            //   Log.e("", "The app moved camera.");
        }
    }

    @Override
    public void onCameraMove() {
        if (onCreateCircle) {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            newRadialDistance = calculateDifferentialDistance(init_cords, bounds.getCenter());// how much has the camera moved
            //    Log.e("newRadialDistance: ", newRadialDistance + "");
            radiusMarker.setTitle("Radius");
            radiusMarker.setSnippet("Meters: " + newRadialDistance);
            radiusMarker.showInfoWindow();
            newRadialDistance = ((newRadialDistance < MINIMUM_RADIUS) ? MINIMUM_RADIUS : newRadialDistance);// I can never remember the ternary operator usage without looking it up
            circle.setRadius(newRadialDistance);

        }
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.e("", "onCameraMoveCanceled");
    }


    @Override
    public void onCircleClick(Circle circle) {
        Log.e("onCircleClick", "circle");
        updateFirebaseGeoDetails(circle);
    }

    private void updateFirebaseGeoDetails(Circle circle) {
        //   set the circle details in the master and toast confirmation to user
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference geocircle = database.getReference("master").child("geocircle");
        geocircle.child("lat").setValue(circle.getCenter().latitude);
        geocircle.child("lon").setValue(circle.getCenter().longitude);
        geocircle.child("radius").setValue(circle.getRadius());
        Toast.makeText(this, "Geo Circle is set to a radius of " + circle.getRadius() + " at latitude " + circle.getCenter().latitude + ", longitude " + circle.getCenter().longitude, Toast.LENGTH_LONG).show();
    }

    private void createNewCircle(int radius) {
        Log.e("createNewCircle", "radius: " + radius);
        //   mMap.clear();
        // house cleaning
        circle.remove();

        // make final blue circle
        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeWidth(4f)
                .strokeColor(Color.argb(100, 63, 45, 255))
                .fillColor(Color.argb(100, 28, 152, 255)));

        circle.setClickable(true);
        //
    }

    @Override
    public void onCameraIdle() {

        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        if (onCreateCircle) {
            newRadialDistance = calculateDifferentialDistance(init_cords, bounds.getCenter());
            //   Log.e("newRadialDistance: ", newRadialDistance + "");
            createNewCircle(newRadialDistance);
            onCreateCircle = false;

            // CameraUpdateFactory.newLatLngBounds(bounds, 100);
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }

}

