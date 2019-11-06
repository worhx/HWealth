package com.team09.hwealth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private ArrayList<String> mStep;
    private ArrayList<String> mDate;
    private Context mContext;

    RecyclerViewAdapter(ArrayList<String> mStep, ArrayList<String> mDate, Context mContext) {
        this.mStep = mStep;
        this.mDate = mDate;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.Date.setText(mDate.get(position));
        holder.Step.setText(mStep.get(position));
    }

    @Override
    public int getItemCount() {
        return mStep.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Date;
        TextView Step;
        RelativeLayout parent_layout;

        ViewHolder(View itemView) {
            super(itemView);
            Step = itemView.findViewById(R.id.recycleLineOne);
            Date = itemView.findViewById(R.id.recycleLineOTwo);
            parent_layout = itemView.findViewById(R.id.recylerView);
        }
    }
}
