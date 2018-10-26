package com.example.vinguyen.assignmentprototype;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.vinguyen.assignmentprototype.Model.Topic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TestTopicsFragment extends Fragment {
    private static final String TAG = "TestTopicsFragment";
    public RecyclerView recyclerView;
    public ArrayList<String> mTopicTitle = new ArrayList<>();
    public ArrayList<Topic> mTopic = new ArrayList<>();
    public ArrayList<String> mScore = new ArrayList<>();
    public ProgressBar progressBarTopics;
    public boolean topicFinished, scoreFinished;

    private OnFragmentInteractionListener mListener;

    public TestTopicsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_test_topics, container, false);
        progressBarTopics = rootView.findViewById(R.id.progressBarTopics);
        recyclerView = rootView.findViewById(R.id.my_recycler_view);
        initTopicArrayList();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    public void initTopicArrayList() {
        //Creates a reference to database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference topicsRef = rootRef.child("INFS3604").child("Topics");
        ValueEventListener topicsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Adds unique topic from firebase to arraylist
                    Topic topic = ds.getValue(Topic.class);
                    if (!mTopicTitle.contains(topic.getTitle())) {
                        mTopicTitle.add(topic.getTitle());
                        topic.setTopicID(ds.getKey());
                        mTopic.add(topic);
                        Log.d(TAG, "onDataChange: topic added");
                    }
                }
                topicFinished = true;
                if (topicFinished && scoreFinished) {
                    initRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //Creates another reference to database under different node
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference scoreRef = rootRef.child("INFS3604").child("Highscore").child(user.getUid());
        ValueEventListener scoreEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Adds score to arraylist
                    String topic = ds.getKey();
                    String score = dataSnapshot.child(topic).getValue().toString();
                    mScore.add(score);
                    Log.d(TAG, "onDataChange: score: " + score + " added");
                }
                scoreFinished = true;
                if (topicFinished && scoreFinished) {
                    initRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        topicsRef.addListenerForSingleValueEvent(topicsEventListener);
        scoreRef.addListenerForSingleValueEvent(scoreEventListener);
    }

    private void initRecyclerView() {
        //Create new instance of Recyclerview adapter and adds to Recyclerview
        TestTopicsRecyclerViewAdapter recyclerViewAdapter = new TestTopicsRecyclerViewAdapter(getActivity(), mTopic, mScore);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBarTopics.setVisibility(View.INVISIBLE);
        Log.d(TAG, "initRecyclerView: RecycleverViewAdapter initialised");
    }
}
