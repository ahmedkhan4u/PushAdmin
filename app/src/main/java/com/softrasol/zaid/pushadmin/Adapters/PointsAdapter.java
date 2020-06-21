package com.softrasol.zaid.pushadmin.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.softrasol.zaid.pushadmin.R;

import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private Context context;
    private List<PointsModel> list;

    public PointsAdapter(Context context, List<PointsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.points_items_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PointsModel model = list.get(position);
        holder.mTxtTitle.setText(model.getTitle());
        holder.mTxtBody.setText(model.getSub_title());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTxtTitle, mTxtBody;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTxtTitle = itemView.findViewById(R.id.points_title);
            mTxtBody = itemView.findViewById(R.id.points_body);
        }
    }
}
