package com.example.vinguyen.assignmentprototype;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TopicsFragment.OnFragmentInteractionListener,
        QuestionsFragment.OnFragmentInteractionListener,
        TestTopicsFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        FlashcardsFragment.OnFragmentInteractionListener,
        TestHistoryFragment.OnFragmentInteractionListener,
        NotesFragment.OnFragmentInteractionListener,
        FlashcardsListFragment.OnFragmentInteractionListener{
    private static final String TAG = "DrawerActivity";
    private FirebaseAuth auth;
    private long timeStart, timeFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        final TextView navUsername = headerView.findViewById(R.id.nav_header_textView);

        //Get instance of user
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //If user is null launches Login activity
                    startActivity(new Intent(DrawerActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //Otherwise sets the email in the drawer
                    Log.d(TAG, "onAuthStateChanged: Current user email: " + user.getEmail());
                    navUsername.setText(user.getEmail());
                }
            }
        };


        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        //Sets default fragment as the Home Fragment
        Fragment fragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment).commitNow();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(DrawerActivity.this, LoginActivity.class));
                finish();
            }
        }


    };

    @Override
    protected void onResume() {
        super.onResume();
        //When app is visible, records the current time in milliseconds
        timeStart = System.currentTimeMillis();

        //Create a reference to database
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("time spent");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    //If user has logged in for the first time, sets time to 0 as no instance of user in database
                    Log.d(TAG, "onDataChange: Time spent set to 0");
                    databaseReference.setValue(Integer.toString(0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //When app is no longer in foreground, records the current time in milliseconds
        timeFinish = System.currentTimeMillis();

        //Calculates time spent in app in milliseconds by subtracting time started and finished
        final long timeSpent = timeFinish - timeStart;

        //Adds time to database so it is cumulative
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("time spent");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Long timeCumulated = Long.parseLong(dataSnapshot.getValue().toString());
                        timeCumulated += timeSpent;
                        databaseReference.setValue(timeCumulated.toString());
                        Log.d(TAG, "onDataChange: Cumulative time set to: " + timeCumulated + " milliseconds");
                    } else {
                        databaseReference.setValue(Long.toString(timeSpent));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_topics) {
            fragment = new TopicsFragment();
        } else if (id == R.id.nav_notes) {
            fragment = new NotesFragment();
        } else if (id == R.id.nav_test) {
            fragment = new TestTopicsFragment();
        } else if (id == R.id.nav_flashcards) {
            fragment = new FlashcardsListFragment();
        } else if (id == R.id.nav_test_history) {
            fragment = new TestHistoryFragment();
        } else if (id == R.id.logout) {
            auth.signOut();
        }

        //Starts fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            Log.d(TAG, "onNavigationItemSelected: started " + fragment.getTag());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
