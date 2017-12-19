package shay.example.com.project_auth.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static shay.example.com.project_auth.SignInActivity.user;
/**
 * Created by Shay on 18/10/2017.
 */

public class FirebaseBackgroundListener extends Service {

    String TAG = "FirebaseBackgroundListener";
   // String userID = PreferenceHelper.getSharedPreferenceString(user.getClass().getA,"ID");//user.getUserId();
    String userID = user.getUserId();
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    DatabaseReference master = db.getReference("master");// listen to changes set by master controller

    Context context;
    @NonNull
    DatabaseReference my_location = db.getReference("users").child(userID).child("location");// listen to changes set in my location

    private boolean isConnected = false;
    private boolean insideGeoCircle = false;
    private boolean isMuted = false;


    private void sendBroadcast(boolean success, boolean isMuted) {
        Intent intent = new Intent("mute_broadcast"); // the filter used when registering the receiver
        intent.putExtra("is_connected", success);
        intent.putExtra("is_muted", isMuted);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);// pop off this local broadcast with intent extras

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context =this;
        userID = intent.getStringExtra("userID");// this is available in the background

        return START_STICKY;

    }
// custom methods from here ...

    // common method to call to set phone audio mute
    private void setPhoneMute(boolean mute) {
        isConnected = true;
        isMuted = false;

            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            assert audio != null;
            int initialVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
            System.out.println("Initial Volume" + initialVolume);

            if (mute) {
                if (!insideGeoCircle) {
                    stopMuteService();
                    return;}// don't continue from here if outside the geo_fenced area
                audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Log.e("onDataChange", "Start Mute Service()");
                //   audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
                //  System.out.println("New volume " + audio.getStreamVolume(AudioManager.STREAM_RING));
                isMuted = true;

            } else if (!mute) {
                stopMuteService();
            }


        DatabaseReference userNode = db.getReference("users").child(userID);// Firebase unique id  userID  => "2W5kiK2UOfaM2XGEF2Brb0AHdMI2"
        userNode.child("muteState").setValue(isMuted);
        sendBroadcast(isConnected, isMuted);
    }
    private void stopMuteService(){

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audio != null;
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        Log.e("onDataChange", "Stop Mute Service(): Vol: " );
        isMuted = false;

        DatabaseReference userNode = db.getReference("users").child(userID);// Firebase unique id  userID  => "2W5kiK2UOfaM2XGEF2Brb0AHdMI2"
        userNode.child("muteState").setValue(isMuted);
        sendBroadcast(isConnected, isMuted);
    }



    private boolean calculateDistanceBetween(DataSnapshot my_location, DataSnapshot geo_circle) {
        // do formulation here ...
        double circle_latitude = (double) geo_circle.child("lat").getValue();
        double circle_longitude = (double) geo_circle.child("lon").getValue();
        long circle_radius = (long) geo_circle.child("radius").getValue();
      //  Log.e("XXXXXXXXX", "" + my_location);

        double my_latitude = (double) my_location.child("latitude").getValue();
        double my_longitude = (double) my_location.child("longitude").getValue();

     //   Log.e("DataSnapshot radius", "" + circle_radius);
        float[] results = new float[1];
        double startLatitude = circle_latitude;// initial start center of geo_circle
        double startLongitude = circle_longitude;
        double endLatitude = my_latitude;// my location
        double endLongitude = my_longitude;

        Location.distanceBetween(startLatitude, startLongitude,
                endLatitude, endLongitude, results);// get the distance and see if its outside the radius value
        int distance = (int) results[0];// cast to clean number
        Log.e("distance fm circ center", distance + "");
        if (distance <= circle_radius) {
            // Inside The Circle
            return true;
        }

        return false;
    }

    private void checkIfOutsideGeoFence(DataSnapshot my_location) {
        final DataSnapshot my_location1 = my_location;
        Log.e("checkIfOutsideGeoFence", "loc: "+ my_location1);
        DatabaseReference geocircle = master.child("geocircle");
        geocircle.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot geo_circle) {
                Log.e("DataSnapshot circle", "" + geo_circle.child("lat").getValue());
                insideGeoCircle = calculateDistanceBetween(my_location1,geo_circle);// use this boolean to determine whether the controller can alter this client mute state


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    @Override
    public void onCreate() {
        super.onCreate();


        ValueEventListener master_switch_connection_handler = new ValueEventListener() {// listening to master switch state
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Boolean result = (Boolean) snapshot.getValue();// cast the snapshot to its original form "boolean"
                setPhoneMute(result);// pass the boolean to set the hardware value of mute


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("onCancelled", "Value: " + databaseError);
                isConnected = false;
                sendBroadcast(isConnected, isMuted);
            }


        };

        ValueEventListener mylocation_connection_handler = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot mylocation) {
                Log.e("DataChange in my loc", "Value: " + mylocation.getValue());// triggered when location client has changed

                checkIfOutsideGeoFence(mylocation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("onCancelled", "Value: " + databaseError);

            }


        };

        master.child("muteService").addValueEventListener(master_switch_connection_handler); // listen to the child muteService boolean state set by the master controller for changes

        my_location.addValueEventListener(mylocation_connection_handler);// listen to my location for changes and check if outside geofence
    }


}
