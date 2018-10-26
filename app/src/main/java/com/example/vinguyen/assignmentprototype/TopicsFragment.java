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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TopicsFragment extends Fragment {
    private static final String TAG = "TopicsFragment";
    private ArrayList<String> mTopicTitle = new ArrayList<>();
    private ArrayList<Topic> mTopic = new ArrayList<>();
    private ProgressBar progressBarTopics;

    private OnFragmentInteractionListener mListener;

    public TopicsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initArrayList();
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        progressBarTopics = view.findViewById(R.id.progressBarTopics);
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

    public void initArrayList() {
        //Create a reference to database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference topicsRef = rootRef.child("INFS3604").child("Topics");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Topic topic = ds.getValue(Topic.class);
                    //Add topic to arraylist
                    if (!mTopicTitle.contains(topic.getTitle())) {
                        Log.d(TAG, "onDataChange: topic title:" + topic.getTitle());
                        mTopicTitle.add(topic.getTitle());
                        topic.setTopicID(ds.getKey());
                        mTopic.add(topic);
                        Log.d(TAG, "onDataChange: topic added");
                    }
                }
                initRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        topicsRef.addListenerForSingleValueEvent(eventListener);
    }

    private void initRecyclerView() {
        //Create new instance of Recyclerview adapter and adds to Recyclerview
        RecyclerView recyclerView = getView().findViewById(R.id.my_recycler_view);
        TopicsRecyclerViewAdapter recyclerViewAdapter = new TopicsRecyclerViewAdapter(getActivity(), mTopic);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBarTopics.setVisibility(View.INVISIBLE);
        Log.d(TAG, "initRecyclerView: RecycleverViewAdapter initialised");
    }
}
