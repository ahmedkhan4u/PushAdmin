package com.softrasol.zaid.pushadmin.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.softrasol.zaid.pushadmin.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private Context context;
    private List<PointsModel> list;

    AlertDialog alert11;

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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final PointsModel model = list.get(position);
        holder.mTxtTitle.setText(model.getTitle());
        holder.mTxtBody.setText(model.getSub_title());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editData(holder, model, position);

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Are you sure you want to delete.");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                list.remove(position);
                                Toast.makeText(context, "item deleted at index "+position , Toast.LENGTH_SHORT).show();

                                new PointsAdapter(context, list);

                                refreshView(position);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

                return true;
            }
        });

    }

    private void editData(final ViewHolder holder, PointsModel model, final int position) {




        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setCancelable(true);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_title_desctption,null);

        builder1.setView(view);

        final EditText mEdtTitle = view.findViewById(R.id.edt_title);
        final EditText mEdtDesc = view.findViewById(R.id.edt_description);

        mEdtTitle.setText(model.getTitle());
        mEdtDesc.setText(model.getSub_title());

        Button mBtnSave = view.findViewById(R.id.btn_save);
        Button mBtnCancel = view.findViewById(R.id.btn_cancel);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert11.cancel();
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String title = mEdtTitle.getText().toString().trim();
                String description = mEdtDesc.getText().toString().trim();

                if (title.isEmpty()){
                    mEdtTitle.setError("Required");
                    mEdtTitle.requestFocus();
                    return;
                }

                if (description.isEmpty()){
                    mEdtDesc.setError("Required");
                    mEdtDesc.requestFocus();
                    return;
                }

                PointsModel model1 = new PointsModel(title, description);
                list.set(position, model1);

                alert11.cancel();
                refreshView(position);

            }
        });


        alert11 = builder1.create();
        alert11.show();


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

    public void refreshView(int position) {
        notifyItemChanged(position);
    }
}
