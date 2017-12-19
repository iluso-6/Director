package shay.example.com.project_auth.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static shay.example.com.project_auth.SignInActivity.user;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
/**
 * Created by Shay on 16/11/2017.
 * updated from depeciated FusedLocationApi to FusedLocationProviderClient
 */


public class LocationService_updated extends Service{


    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10000;  /* 30 secs */
    private long FASTEST_INTERVAL = 10000; /* 20 sec */



    private String TAG = "ConnectionService";
    private Context context;


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        Log.e(TAG, "onStartCommand");
        if (checkPermissions(context)) {
            startLocationUpdates(context);
        }
        return START_STICKY;
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates(Context context) {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // get the last updated location
                     Log.e("onLocationResult","onLocationResult"+locationResult.getLastLocation());
                     updateFireBase(locationResult);
                    }
                },
                Looper.myLooper());
    }

    private void updateFireBase(LocationResult locationResult) {
        Location result = locationResult.getLastLocation();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference userNode = database.getReference("users").child(user.getUserId());

        userNode.child("lat").setValue(result.getLatitude());
        userNode.child("lon").setValue(result.getLongitude());
        userNode.child("location").setValue(result);

        // update the User Class
        user.setLatitude(result.getLatitude());
        user.setLongitude(result.getLongitude());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public boolean checkPermissions(Context context) {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }



}