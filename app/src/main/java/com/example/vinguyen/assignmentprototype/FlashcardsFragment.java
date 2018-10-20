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

public class FlashcardsFragment extends Fragment {
    private static final String TAG = "ContentsFragment";
    private RecyclerView recyclerView;
    private ArrayList<String> mTopicTitle = new ArrayList<>();
    private ArrayList<Topic> mTopic = new ArrayList<>();
    private ArrayList<String> mScore = new ArrayList<>();
    private ProgressBar progressBarTopics;
    private boolean topicFinished, scoreFinished;

    private OnFragmentInteractionListener mListener;

    public FlashcardsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_flashcards, container, false);
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
        Log.d(TAG, "initTopicArrayList: called");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference topicsRef = rootRef.child("INFS3604").child("Topics");
        ValueEventListener topicsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Topic topic = ds.getValue(Topic.class);
                    if (!mTopicTitle.contains(topic.getTitle())) {
                        mTopicTitle.add(topic.getTitle());
                        topic.setTopicID(ds.getKey());
                        mTopic.add(topic);
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference scoreRef = rootRef.child("INFS3604").child("Highscore").child(user.getUid());
        ValueEventListener scoreEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String topic = ds.getKey();
                    mScore.add(dataSnapshot.child(topic).getValue().toString());
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
        //recyclerView = getView().findViewById(R.id.my_recycler_view);
        FlashcardsRecyclerViewAdapter recyclerViewAdapter = new FlashcardsRecyclerViewAdapter(getActivity(), mTopic, mScore);
        Log.d(TAG, "initRecyclerView: called");
        recyclerView.setAdapter(recyclerViewAdapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new SnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        progressBarTopics.setVisibility(View.INVISIBLE);
    }
}
