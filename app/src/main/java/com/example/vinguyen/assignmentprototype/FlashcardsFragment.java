package com.example.vinguyen.assignmentprototype;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vinguyen.assignmentprototype.Model.Flashcard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FlashcardsFragment extends Fragment {
    private static final String TAG = "FlashcardsFragment";
    private RecyclerView recyclerView;
    private ArrayList<Flashcard> mFlashcard = new ArrayList<>();
    private ProgressBar progressBarTopics;
    private String mTopicID = "", mTopicTitle;
    private TextView textViewTitle;
    private Button btnReset;

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
        textViewTitle = rootView.findViewById(R.id.textViewTitle);
        btnReset = rootView.findViewById(R.id.btnReset);

        //Recieves topic ID and the title of topic from prior fragment
        mTopicID = getArguments().getString("topicID");
        mTopicTitle = getArguments().getString("title");
        Log.d(TAG, "onCreateView: recieved ID:" + mTopicID + ", Title: " + mTopicTitle);

        textViewTitle.setText(mTopicTitle);

        //Initialises array list/recyclerview
        initFlashcardArrayList();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Resets flashcards
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFlashcardArrayList();
                Log.d(TAG, "onClick: flashcards reset");
            }
        });
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

    //Initialises flashcards
    public void initFlashcardArrayList() {
        //Create a reference to database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference flashcardsRef = rootRef.child("INFS3604").child("Topics").child(mTopicID).child("flashcards");
        ValueEventListener flashcardsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //For each node in Firebase under reference, adds key as front of flashcard and value as back of flashcard to arraylist
                    String front = ds.getKey();
                    String back = ds.getValue().toString();
                    mFlashcard.add(new Flashcard(front, back));
                    Log.d(TAG, "onDataChange: Flashcard added front: " + front + " and back: " + back);

                    //Uses Collections to shuffle the Arraylist
                    Collections.shuffle(mFlashcard);
                }
                initRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        flashcardsRef.addListenerForSingleValueEvent(flashcardsEventListener);
    }

    private void initRecyclerView() {
        //Create new instance of Recyclerview adapter and adds to Recyclerview
        FlashcardsRecyclerViewAdapter recyclerViewAdapter = new FlashcardsRecyclerViewAdapter(getActivity(), mFlashcard);
        recyclerView.setAdapter(recyclerViewAdapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //Sets recycler view to snap to screen, i.e. doesn't stay in the middle of two flashcards
        recyclerView.setOnFlingListener(null);
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(recyclerView);
        progressBarTopics.setVisibility(View.INVISIBLE);
        Log.d(TAG, "initRecyclerView: RecycleverViewAdapter initialised");
    }
}
