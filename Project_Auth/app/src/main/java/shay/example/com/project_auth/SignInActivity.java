package shay.example.com.project_auth;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import shay.example.com.project_auth.helpers.MasterDetails;
import shay.example.com.project_auth.helpers.PreferenceHelper;
import shay.example.com.project_auth.helpers.User;
import shay.example.com.project_auth.helpers.Utility;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity Log:";

    public GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private Button changeAccountBtn;
    private Button nextBtn;
    private ImageView imageOne;
    private FirebaseAuth mAuth;
    private FirebaseUser fuser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private MediaPlayer mp;

    private String masterName;
    private String masterUrl;

    public static User user;
    public static MasterDetails masterDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        // my google sign in button
        signInButton = (SignInButton) findViewById(R.id.googleSignInBtn);
        changeAccountBtn = (Button) findViewById(R.id.changeAccountBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        updateDisplayState(false);


        mAuth = FirebaseAuth.getInstance();

        changeAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase sign out
                mAuth.signOut();

                // Google sign out
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                // reload the activity to get new Sign in credential
                                Intent reload = new Intent(SignInActivity.this, SignInActivity.class);
                                startActivity(reload);
                                finish();
                            }
                        });

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    // changeAccountBtn = findViewById(R.id.changeAccountBtn);
                    // user is already signed in
                    signInButton.setVisibility(View.INVISIBLE);
                    changeAccountBtn.setVisibility(View.VISIBLE);

                    nextBtn.setVisibility(View.VISIBLE);
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeAccountBtn.setEnabled(false); // disable this for safety
                            // the user is requesting to proceeed
                            mp.start();
                        }
                    });
                    updateUserImageDetails();
                    updateMasterDetails(); // get the info from firebase on master user name,url etc..
                    updateDisplayState(true);// set the first stage to completed


                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {


                            Intent intentMain = new Intent(SignInActivity.this, Locations_Activity.class);


                            //overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                            // slide animation between activities in Bundle - more reliable

                            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                            startActivity(intentMain, animation);
                            SignInActivity.this.finish();
                            mp.release();
                        }
                    });


                }
            }
        };


        mp = MediaPlayer.create(this, R.raw.accept_1);

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        //access the google play services
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(getApplicationContext(), "onConnectionFailed: " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                //  .addApi(LocationServices.API)
                .build();// build the sign in options dialog


        //  AppCommon inst = new AppCommon();
        //   inst.setClient(mGoogleApiClient);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();// start the sign in intent

                Log.e("onClick", "Sign in");
            }
        });
    }

    private void updateDisplayState(boolean complete) {
        if (!complete) {
            // animate the current progress state
            final Animation zoom = AnimationUtils.loadAnimation(getBaseContext(), R.anim.repeatzoom);
            imageOne = findViewById(R.id.img1);

            imageOne.startAnimation(zoom);
        } else {

            // set the used color
            imageOne.setColorFilter(Utility.ARGB);
            imageOne.clearAnimation();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.e("result", "isSuccess");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e("result", "Google Sign In failed");
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {


        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            updateUserImageDetails();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //    updateUI(null);
                        }

                        // ...
                    }
                });
    }
    private void updateMasterDetails() {

        DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference("master");

// Read from the database
        masterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                masterName = (String) dataSnapshot.child("name").getValue();// populated the details for the custom dialog
                masterUrl = (String) dataSnapshot.child("url").getValue();

                masterDetails = new MasterDetails();
                masterDetails.setName(masterName);
                masterDetails.setUrl(masterUrl);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });



    }



    private void updateUserImageDetails() {

        // get the user logged in details from Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        String photoUrl = String.valueOf(currentUser.getPhotoUrl());

        currentUser.getIdToken(true);
        String userId = currentUser.getUid();// FirebaseUser.getToken()

        // update the user class
        user = new User();
        user.setUserName(name);
        user.setEmail(email);
        user.setPhotoUrl(photoUrl);
        user.setUserId(userId);


        PreferenceHelper.setSharedPreferenceString(getApplicationContext(),"ID",userId);
        TextView title = (TextView) findViewById(R.id.title_text);
        title.setText("Welcome " + "\n" + name);

        ImageView userImg = (ImageView) findViewById(R.id.user_img);


        Picasso.with(getApplicationContext()).load(photoUrl).into(userImg); // a lovely image downloading and caching library for Android

    }


}


