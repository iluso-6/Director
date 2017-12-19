package shay.example.com.project_auth;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Timer;

import shay.example.com.project_auth.helpers.User;
import shay.example.com.project_auth.helpers.Utility;
import shay.example.com.project_auth.services.FirebaseBackgroundListener;

import static shay.example.com.project_auth.SignInActivity.user;


public class FirebaseConnectionActivity extends AppCompatActivity {

    private User userRef;
    MediaPlayer mp;
    Timer timer;
    ImageView imageOne;
    ImageView imageTwo;
    ImageView imageThree;
    int ON_DO_NOT_DISTURB_CALLBACK_CODE = 9898;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_com);
        imageOne =  findViewById(R.id.img1);
        imageTwo =  findViewById(R.id.img2);
        imageThree = findViewById(R.id.img3);
        userRef = user;
        updateDisplayState();
        updateTopBarAnim(false);
        requestMutePhonePermsAndMutePhone();


        mp = MediaPlayer.create(this, R.raw.accept_3);

    }


    // MODIFY_AUDIO_SETTINGS  not a dangerous permission but Do not Disturb IS!

    private void requestMutePhonePermsAndMutePhone() {
        try {
            if (Build.VERSION.SDK_INT < 23) {// no runtime permissions needed here ...
                connectToFirebase();
            } else if( Build.VERSION.SDK_INT >= 23 ) {
                this.requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp();
            }
        } catch ( SecurityException e ) {

        }
    }

    private void requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp() {
        //TO SUPPRESS API ERROR MESSAGES
        if( Build.VERSION.SDK_INT < 23 ) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notificationManager.isNotificationPolicyAccessGranted()) {
            connectToFirebase(); // continue with connection
        } else{
            // Ask the user to grant access
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult( intent, ON_DO_NOT_DISTURB_CALLBACK_CODE );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ON_DO_NOT_DISTURB_CALLBACK_CODE ) {
            this.requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp();
        }
    }

    private void connectToFirebase() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userNode = database.getReference("users").child(userRef.getUserId());// reference



        userNode.child("name").setValue(userRef.getUserName());
        userNode.child("email").setValue(userRef.getEmail());
        userNode.child("url").setValue(userRef.getPhotoUrl());


        Intent intent = new Intent(this, FirebaseBackgroundListener.class);
        intent.putExtra("userID",userRef.getUserId()); // get the unique user to pass as it will not be available in the background
        startService(intent); //start the background service with broadcaster


    }
// BroadcastReceiver to listen to the FirebaseBackground Listener
    // this will give us updates on the background listener states is_muted, is_connected
    private BroadcastReceiver bReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {


            final boolean is_connected = intent.getBooleanExtra("is_connected", false);
            final boolean is_muted = intent.getBooleanExtra("is_muted", false);
            Toast.makeText(getApplicationContext(), "Connected: " + is_connected, Toast.LENGTH_LONG).show();
                 if(is_connected){
                     FirebaseDatabase database = FirebaseDatabase.getInstance();
                     DatabaseReference userNode = database.getReference("users").child(userRef.getUserId());
                     userNode.child("muteState").setValue(is_muted);// set the individual mute state in the data base

            updateTopBarAnim(true);// finish top bar anim
            TextView ctrl_text = (TextView) findViewById(R.id.controller_text);
            ctrl_text.setText(R.string.connected);

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                   //  finish up by bringing up the home screen
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                 //   finish();// close the application
                }
            });
            mp.start();
        }else{
                     Toast.makeText(getApplicationContext(), "Not Connected: " + is_connected, Toast.LENGTH_LONG).show();
        }
        }
    };

    private void updateDisplayState() {


        // set the used color
        imageOne.setColorFilter(Utility.ARGB);
        imageTwo.setColorFilter(Utility.ARGB);

        // set the User image
        ImageView userImg =  findViewById(R.id.user_img);
        Picasso.with(getApplicationContext()).load(userRef.getPhotoUrl()).into(userImg); //  image downloading and caching library for Android
    }

    private void updateTopBarAnim(boolean complete){



        if(!complete) {
            // animate the current progress state
            final Animation zoom = AnimationUtils.loadAnimation(getBaseContext(), R.anim.repeatzoom);
            imageThree.startAnimation(zoom);
        }else{

            // set the used color
            imageThree.setColorFilter(Utility.ARGB);
            imageThree.clearAnimation();
        }

    }

    protected void onResume(){
        super.onResume();
        Log.e("onResume","registerReceiver");
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("mute_broadcast"));
    }

    protected void onPause (){
        super.onPause();
        Log.e("onPause","un registerReceiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy","un registerReceiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
        this.finish();
    }
}
