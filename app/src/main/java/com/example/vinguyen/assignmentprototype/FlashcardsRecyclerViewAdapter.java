package com.example.vinguyen.assignmentprototype;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinguyen.assignmentprototype.Model.Flashcard;
import com.example.vinguyen.assignmentprototype.Model.Topic;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;

public class FlashcardsRecyclerViewAdapter extends RecyclerView.Adapter<FlashcardsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "FlashcardsRecyclerView";
    private ArrayList<Flashcard> mFlashcard = new ArrayList<>();
    private Context mContext;

    public FlashcardsRecyclerViewAdapter(Context mContext, ArrayList<Flashcard> mFlashcard) {
        this.mFlashcard = mFlashcard;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_flashcards, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //Sets the front and back of flashcard based on arraylist sent from fragment
        String frontText = mFlashcard.get(position).getFront();
        String backText = mFlashcard.get(position).getBack();

        holder.textViewFlashcardFront.setText(frontText);
        holder.textViewFlashcardBack.setText(backText);
        Log.d(TAG, "onBindViewHolder: RecyclerView set flashcard front: " + frontText + " back: " + backText);
    }

    @Override
    public int getItemCount() {
        return mFlashcard.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewFlashcardFront, textViewFlashcardBack;
        ConstraintLayout backSide, frontSide;
        EasyFlipView easyFlipView;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewFlashcardBack = itemView.findViewById(R.id.textViewFlashcardBack);
            textViewFlashcardFront = itemView.findViewById(R.id.textViewFlashcardFront);

            easyFlipView = itemView.findViewById(R.id.easyFlipView);
            backSide = itemView.findViewById(R.id.backSide);
            frontSide = itemView.findViewById(R.id.frontSide);

            //If flashcard clicked, changes front to back and vice versa in 400 milliseonds
            View.OnClickListener flipClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    easyFlipView.setFlipDuration(400);
                    easyFlipView.flipTheView();
                }
            };
            easyFlipView.setOnClickListener(flipClickListener);
        }
    }

}
