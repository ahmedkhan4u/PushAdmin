package com.softrasol.zaid.pushadmin.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softrasol.zaid.pushadmin.ChallengesActivity;
import com.softrasol.zaid.pushadmin.ExerciseActivity;
import com.softrasol.zaid.pushadmin.MobilityActivity;
import com.softrasol.zaid.pushadmin.NutritionActivity;
import com.softrasol.zaid.pushadmin.R;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FitBodyFragment extends Fragment {


    public FitBodyFragment() {
        // Required empty public constructor
    }

    private View mView;
    private RelativeLayout mChallenges, mExercise, mMobility, mDetails, mPrograms, mNutrition;
    AlertDialog alert11;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_fit_body, container, false);

        widgetsInitailization();
        mobilityClick();
        challengesClick();
        exerciseClick();
        nutritionClick();

        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialogDetails("fitbody", "home_screen_details");

            }
        });

        mPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialogDetails("fitbody", "programs");
            }
        });



        return mView;
    }

    private void nutritionClick() {

        mNutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NutritionActivity.class));
            }
        });

    }

    private void exerciseClick() {
        mExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ExerciseActivity.class));
            }
        });
    }

    private void challengesClick() {
        mChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChallengesActivity.class));
            }
        });
    }

    private void mobilityClick() {
        mMobility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MobilityActivity.class));
            }
        });
    }

    private void widgetsInitailization() {

        mChallenges = mView.findViewById(R.id.fitbody_challenges);
        mExercise = mView.findViewById(R.id.fitbody_exercise);
        mMobility = mView.findViewById(R.id.fitbody_mobility);
        mPrograms = mView.findViewById(R.id.fitbody_programs);
        mNutrition = mView.findViewById(R.id.fitbody_nutrition);

        mDetails = mView.findViewById(R.id.fitbody_details);
    }

    private void uploadDialogDetails(final String collectionName, final String documentName) {


        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setCancelable(true);

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_title_desctption,null);

        builder1.setView(view);

        final EditText mEdtTitle = view.findViewById(R.id.edt_title);
        final EditText mEdtDesc = view.findViewById(R.id.edt_description);

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



                CollectionReference collectionReference = FirebaseFirestore.getInstance()
                        .collection(collectionName);

                DocumentReference documentReference = collectionReference.document(documentName);

                Map map = new HashMap();
                map.put("title", title);
                map.put("description", description);

                documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Details Saved", Toast.LENGTH_SHORT).show();
                            alert11.cancel();
                        } else {
                            Toast.makeText(getActivity(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            alert11.cancel();
                        }
                    }
                });

            }
        });


        alert11 = builder1.create();
        alert11.show();

    }


}
