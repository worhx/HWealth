package com.team09.hwealth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG ="RecyclerViewAdapter";
    private ArrayList<String> mStep = new ArrayList<>();
    private ArrayList<String> mDate = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<String> mStep, ArrayList<String> mDate, Context mContext) {
        this.mStep = mStep;
        this.mDate = mDate;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder=  new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder");
        holder.Date.setText(mDate.get(position));
        holder.Step.setText(mStep.get(position));
    }

    @Override
    public int getItemCount() {
        return mStep.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView Date;
        TextView Step;
        RelativeLayout parent_layout;
        public ViewHolder(View itemView) {
            super(itemView);
            Step = itemView.findViewById(R.id.recycleLineOne);
            Date = itemView.findViewById(R.id.recycleLineOTwo);
            parent_layout = itemView.findViewById(R.id.recylerView);
        }
    }
}
