package shay.example.com.director_master;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import helper_classes.PreferenceHelper;


public class MainActivity extends AppCompatActivity {

    public static final String MONITOR_SWITCH = "switch";// another child key for sharedPrefs
    public static final boolean defaultMonitorState = false;// another child key for sharedPrefs
    protected SwitchCompat sw;

    private DatabaseReference muteRef, usersRef;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert switch
        actionBar.setCustomView(R.layout.switch_layout);
        actionBar.setIcon(R.mipmap.ic_launcher);// display custom icon in toolbar

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

        final FloatingActionButton myFab = findViewById(R.id.fab);

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("onClick", "myFab");
                Intent intentMaps = new Intent(MainActivity.this,MapsActivity.class);

                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                startActivity(intentMaps, animation);
                MainActivity.this.finish();
              /*  ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(MainActivity.this,
                                v.findViewById(R.id.fab),
                                "simple_activity_transition");
                startActivity(intentMaps, options.toBundle());*/
            }
        });

        //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();

        muteRef = database.getReference("master").child("muteService");

        usersRef = database.getReference("users");

        // Toast.makeText(getApplicationContext(),"Ref: "+usersRef.getRef(),Toast.LENGTH_LONG).show();
        usersRef.keepSynced(true);

        boolean switchState = PreferenceHelper.getSharedPreferenceBoolean(this, MainActivity.MONITOR_SWITCH, MainActivity.defaultMonitorState);

        sw = findViewById(R.id.customSwitch);
        sw.setChecked(switchState); // set the physical switch state in on Create to sync with the stored state
        setSwitchState(switchState); // update the switch text

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setSwitchState(isChecked);
            }

        });

        recyclerView = findViewById(R.id.mainRecyclerView);


        //   recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }


    private void setSwitchState(boolean isChecked) {

        if (isChecked) {
            sw.setText(R.string.mute_on);// update the text
        } else {
            sw.setText(R.string.mute_off);

        }
        PreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), MainActivity.MONITOR_SWITCH, isChecked);
        updateFireBaseDB(isChecked);
    }

    private void updateFireBaseDB(boolean isChecked) {
        muteRef.setValue(isChecked);

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<User, myViewHolder> adapter = new FirebaseRecyclerAdapter<User, myViewHolder>(


                User.class,
                R.layout.activity_card,
                myViewHolder.class,
                usersRef
        ) {
            @Override
            protected void populateViewHolder(myViewHolder viewHolder, User model, int position) {


                viewHolder.setTitle(model.getName());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setState(model.getMuteString());
                viewHolder.setImage(MainActivity.this, model.getUrl());

            }
        };

        recyclerView.setAdapter(adapter);


    }


    public static class myViewHolder extends RecyclerView.ViewHolder {

        //   private final TextView textViewName;
        //    private final TextView textViewEmail;
        //   private final ImageView imageView;
        private final View mView;
        private CardView cardView;
        //   private final TextView data_content;

        public myViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            //    data_content = mView.findViewById(R.id.content);
            //    textViewName = itemView.findViewById(android.R.id.text1);
            //     textViewEmail = itemView.findViewById(android.R.id.text2);
            //    imageView = itemView.findViewById(android.R.id.icon);
        }

        public void setImage(Context context, String image) {
            ImageView data_image = mView.findViewById(R.id.userImg);
            Picasso.with(context).load(image).into(data_image);
        }

        public void setTitle(String title) {
            TextView data_title = mView.findViewById(R.id.titleText);
            data_title.setText(title);
        }


        public void setEmail(String userEmail) {
            TextView email = mView.findViewById(R.id.email);
            email.setText(userEmail);
        }

        public void setState(String state) {
            ImageView mute_state = mView.findViewById(R.id.status);
            if (state.equalsIgnoreCase("true")) {
                mute_state.setImageResource(R.drawable.mute_speaker);// show the muted icon
            } else {
                mute_state.setImageResource(R.drawable.norm_speaker);
            }
        }

    }

}