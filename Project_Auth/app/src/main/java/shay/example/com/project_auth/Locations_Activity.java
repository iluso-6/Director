package shay.example.com.project_auth;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import shay.example.com.project_auth.helpers.Utility;
import shay.example.com.project_auth.services.LocationService_updated;

import static shay.example.com.project_auth.SignInActivity.masterDetails;
import static shay.example.com.project_auth.SignInActivity.user;

public class Locations_Activity extends AppCompatActivity { //location/display-address use the location intentService

    private static final String TAG = "LocationsActivity : ";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private ImageView imageTwo;
    private MediaPlayer mp;
    private DatabaseReference masterRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        updateDisplayState(false);

        mp = MediaPlayer.create(this, R.raw.accept_2);


    }


    private void updateDisplayState(boolean complete) {
        ImageView imageOne = (ImageView) findViewById(R.id.img1);
        // set the used color

        imageOne.setColorFilter(Utility.ARGB);

        // set the User image
        ImageView userImg = (ImageView) findViewById(R.id.user_img);
        Picasso.with(getApplicationContext()).load(user.getPhotoUrl()).into(userImg); //  image downloading and caching library for Android

        if (!complete) {
            // animate the current progress state
            final Animation zoom = AnimationUtils.loadAnimation(getBaseContext(), R.anim.repeatzoom);
            imageTwo = (ImageView) findViewById(R.id.img2);

            imageTwo.startAnimation(zoom);
        } else {

            // set the used color
            imageTwo.setColorFilter(Utility.ARGB);
            imageTwo.clearAnimation();
            TextView locationText = (TextView) findViewById(R.id.location_text);
            locationText.setText(R.string.location_found_text);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startLocationService();


        }
    }

    /**

     */
    @SuppressWarnings("MissingPermission")
    private void startLocationService() {

        startService(new Intent(this, LocationService_updated.class)); //start the location  service
        updateDisplayState(true);// finish the animation
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                Intent intent = new Intent(Locations_Activity.this, FirebaseConnectionActivity.class);
                //overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                // slide animation between activities in Bundle - more reliable
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                startActivity(intent, animation);
                Locations_Activity.this.finish();
                mp.release();
            }
        });


        mp.start();// start the completed sound and launch activity

    }


    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // launch the default permissions dialog
    private void startLocationPermissionRequest() {


        ActivityCompat.requestPermissions(Locations_Activity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // this custom dialog will personalise the permissions request prior to calling them ie. your boss needs permission to ...
    private void showCustomDialog() {

        ImageView image = new ImageView(this);

        Picasso.with(getApplicationContext()).load(masterDetails.getUrl()).into(image);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this)
                        .setTitle("Director Master")
                        .setMessage("The Master operator, " + masterDetails.getName() + " will require permissions from you, please accept.\n")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startLocationPermissionRequest();// start the actual permissions request
                            }
                        }).
                        setView(image);
        builder.setIcon(R.drawable.master_icon);
        builder.create().show();
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.e(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            showCustomDialog();
                        }
                    });

        } else {
            Log.e(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            showCustomDialog();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                startLocationService();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}

