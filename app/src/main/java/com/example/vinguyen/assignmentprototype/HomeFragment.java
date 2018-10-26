package com.example.vinguyen.assignmentprototype;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private OnFragmentInteractionListener mListener;
    private ArrayList<String> mScore = new ArrayList<>();
    private Integer countPerfect = 0;
    private ImageView imageViewTree, imageViewBunny1, imageViewBunny2, imageViewBunny3, imageViewBunny4, imageViewBunny5, imageViewBunny6;
    private ProgressBar progressBar;
    private Boolean bunnyLoaded = false, treeLoaded = false;
    private TextView textViewTimeSpent, textViewQuizzes;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        textViewQuizzes = view.findViewById(R.id.textViewQuizzes);
        textViewTimeSpent = view.findViewById(R.id.textViewTimeSpent);
        imageViewTree = view.findViewById(R.id.imageViewTree);
        imageViewBunny1 = view.findViewById(R.id.imageViewBunny1);
        imageViewBunny2 = view.findViewById(R.id.imageViewBunny2);
        imageViewBunny3 = view.findViewById(R.id.imageViewBunny3);
        imageViewBunny4 = view.findViewById(R.id.imageViewBunny4);
        imageViewBunny5 = view.findViewById(R.id.imageViewBunny5);
        imageViewBunny6 = view.findViewById(R.id.imageViewBunny6);
        progressBar = view.findViewById(R.id.progressBar);

        //Create a reference to database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference scoreRef = rootRef.child("INFS3604").child("Highscore").child(user.getUid());
        ValueEventListener scoreEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Adds each score pulled from Firebase node of user to array
                    String topic = ds.getKey();
                    mScore.add(dataSnapshot.child(topic).getValue().toString());
                }
                for (int i=0; i<mScore.size(); i++) {
                    //Counts how many perfects scores of 4 are included in array
                    if (Integer.parseInt(mScore.get(i)) == 4) {
                        countPerfect++;
                    }
                }
                Log.d(TAG, "onDataChange: No. of perfect quizzes: " + countPerfect);
                textViewQuizzes.setText("Perfect Quizzes: " + countPerfect + "/" + mScore.size());
                switchBunny();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //Create another reference to database
        final DatabaseReference timeRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("time spent");
        timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //Pulls accumulated time from Firebase node of user and converts to minutes if not 0
                    Long timeCumulated = Long.parseLong(dataSnapshot.getValue().toString());
                    final int minutes = (int) ((timeCumulated/ 1000) / 60);
                    Log.d(TAG, "onDataChange: Amount of time spent in app: " + minutes);
                    textViewTimeSpent.setText("Time spent (Refreshes on restart): " + minutes + " minutes");

                    switchTree(minutes);
                } else {
                    Log.d(TAG, "onDataChange: 0 time spent in app");
                    textViewTimeSpent.setText("Time spent (Refreshes on restart): 0 minutes");
                    switchTree(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        scoreRef.addListenerForSingleValueEvent(scoreEventListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //Changes visibility of bunnies depending on perfect quizzes
    public void switchBunny(){
        Activity activity = getActivity();
        if(activity != null) {
            if (countPerfect > 0) {
                imageViewBunny1.setVisibility(View.VISIBLE);
            }
            if (countPerfect > 1) {
                imageViewBunny2.setVisibility(View.VISIBLE);
            }
            if (countPerfect > 2) {
                imageViewBunny3.setVisibility(View.VISIBLE);
            }
            if (countPerfect > 3) {
                imageViewBunny4.setVisibility(View.VISIBLE);
            }
            if (countPerfect > 4) {
                imageViewBunny5.setVisibility(View.VISIBLE);
            }
            imageViewBunny6.setVisibility(View.VISIBLE);
            bunnyLoaded = true;
            Log.d(TAG, "switchBunny: bunnies changed");
            switchProgressBar();
        }
    }

    //Changes image of tree depending on accumulated time
    public void switchTree(Integer minutes){
        Activity activity = getActivity();
        if(activity != null) {
            if (minutes >= 20) {
                imageViewTree.setImageDrawable(getResources().getDrawable(R.drawable.apple_tree2));
            }
            if (minutes >= 40) {
                imageViewTree.setImageDrawable(getResources().getDrawable(R.drawable.apple_tree3));
            }
            if (minutes >= 60) {
                imageViewTree.setImageDrawable(getResources().getDrawable(R.drawable.apple_tree4));
            }
            if (minutes >= 80) {
                imageViewTree.setImageDrawable(getResources().getDrawable(R.drawable.apple_tree5));
            }
            imageViewTree.setVisibility(View.VISIBLE);
            treeLoaded = true;
            Log.d(TAG, "switchTree: tree changed");
            switchProgressBar();
        }
    }

    //Checks to see if both bunnies and trees are loaded before removing progress bar
    public void switchProgressBar(){
        if (treeLoaded && bunnyLoaded) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
