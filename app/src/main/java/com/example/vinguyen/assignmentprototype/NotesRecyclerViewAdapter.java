package com.example.vinguyen.assignmentprototype;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vinguyen.assignmentprototype.Model.Topic;

import java.util.ArrayList;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "NotesRecyclerViewAdapter";
    private ArrayList<String> mNotes = new ArrayList<>();
    private Context mContext;

    public NotesRecyclerViewAdapter(Context mContext, ArrayList<String> mNotes) {
        this.mContext = mContext;
        this.mNotes = mNotes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notes, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.notes.setText(mNotes.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView notes;

        public ViewHolder(View itemView) {
            super(itemView);
            notes = itemView.findViewById(R.id.notes);
        }
    }

}
