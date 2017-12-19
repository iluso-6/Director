package shay.example.com.director_master;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import helper_classes.Master;
import helper_classes.Utility;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity Log:";
    public static Master master;
    public GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private Button changeAccountBtn;
    private Button nextBtn;
    private ImageView imageOne;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        // my google sign in button
        signInButton = findViewById(R.id.googleSignInBtn);
        changeAccountBtn = findViewById(R.id.changeAccountBtn);
        nextBtn = findViewById(R.id.nextBtn);
        updateDisplayState(false);

        mp = MediaPlayer.create(this, R.raw.accept_1);
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
                    // master is already signed in
                    signInButton.setVisibility(View.INVISIBLE);
                    changeAccountBtn.setVisibility(View.VISIBLE);

                    nextBtn.setVisibility(View.VISIBLE);
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeAccountBtn.setEnabled(false); // disable this for safety
                            // the master is requesting to proceeed
                            mp.start();
                        }
                    });
                    updateUserImageDetails(); // update the class locally
                    updateFirebaseDetails(); // update firebase with the class details
                    updateDisplayState(true);// set the first stage to completed


                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {


                            Intent intentMain = new Intent(SignInActivity.this, MainActivity.class);


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

    private void updateFirebaseDetails() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference masterNode = database.getReference("master");

// no nesting used here by design, overwrite previous master user details
        masterNode.child("name").setValue(master.getName());
        masterNode.child("email").setValue(master.getEmail());
        masterNode.child("url").setValue(master.getUrl());

        masterNode.child("createdAt").setValue(ServerValue.TIMESTAMP);


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
                            // Sign in success, update UI with the signed-in master's information
                            Log.e(TAG, "signInWithCredential:success");
                            updateUserImageDetails();
                        } else {
                            // If sign in fails, display a message to the master.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //    updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUserImageDetails() {

        // get the master logged in details from Firebase
        FirebaseUser masterUser = mAuth.getCurrentUser();
        String name = masterUser.getDisplayName();
        String email = masterUser.getEmail();
        String photoUrl = String.valueOf(masterUser.getPhotoUrl());

        String masterId = masterUser.getUid();// FirebaseUser.getToken()

        // update the master class
        master = new Master();
        master.setName(name);
        master.setEmail(email);
        master.setUrl(photoUrl);
        master.setMasterID(masterId);

        TextView title = findViewById(R.id.title_text);
        title.setText("Welcome " + "\n" + name);

        ImageView userImg = findViewById(R.id.user_img);


        Picasso.with(getApplicationContext()).load(photoUrl).into(userImg); // a lovely image downloading and caching library for Android

    }


}


