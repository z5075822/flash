package com.example.vinguyen.assignmentprototype;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinguyen.assignmentprototype.Model.Topic;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Topic> mContent = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context mContext, ArrayList<Topic> mContent) {
        this.mContent = mContent;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_subsection, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called ");
        holder.content.setText(mContent.get(position).getTitle());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity fragment = new MainActivity();
                Bundle args = new Bundle();
                args.putString("Key", mContent.get(position).getTopicID());
                FragmentManager manager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                fragment.setArguments(args);
                manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("topics").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView content;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.contents);
            parentLayout = itemView.findViewById(R.id.parent_layout);


        }
    }

}
