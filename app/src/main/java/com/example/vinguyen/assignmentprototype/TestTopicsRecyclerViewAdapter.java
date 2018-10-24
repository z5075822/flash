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

import com.example.vinguyen.assignmentprototype.Model.Topic;

import java.util.ArrayList;

public class TestTopicsRecyclerViewAdapter extends RecyclerView.Adapter<TestTopicsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "TestTopicsRecyclerViewA";
    private ArrayList<Topic> mContent = new ArrayList<>();
    private ArrayList<String> mScore = new ArrayList<>();
    private Context mContext;
    private Integer totalQuestions = 4;

    public TestTopicsRecyclerViewAdapter(Context mContext, ArrayList<Topic> mContent, ArrayList<String> mScore) {
        this.mContent = mContent;
        this.mContext = mContext;
        this.mScore = mScore;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_test_topic, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.content.setText(mContent.get(position).getTitle());
        holder.score.setText(mScore.get(position).toString() + "/" + totalQuestions);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionsFragment fragment = new QuestionsFragment();
                Bundle args = new Bundle();
                args.putString("Key", mContent.get(position).getTopicID());
                FragmentManager manager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                fragment.setArguments(args);
                manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("testTopics").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView content;
        TextView score;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.contents);
            score = itemView.findViewById(R.id.score);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
