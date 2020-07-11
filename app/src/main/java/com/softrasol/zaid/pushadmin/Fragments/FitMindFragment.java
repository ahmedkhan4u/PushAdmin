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
import com.softrasol.zaid.pushadmin.MeditationActivity;
import com.softrasol.zaid.pushadmin.MindsetActivity;
import com.softrasol.zaid.pushadmin.R;
import com.softrasol.zaid.pushadmin.UploadVideoActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FitMindFragment extends Fragment {


    public FitMindFragment() {
        // Required empty public constructor
    }

    private View mView;
    private RelativeLayout mBreathout, mMotivation, mMindset,
            mMeditation, mUserNameBg, mFitMindDetails, mFitMindPrograms;
    AlertDialog alert11;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_fit_mind, container, false);

        widgetsInitailization();

        breathoutClick();
        motivationClick();
        mindsetClick();
        meditationClick();
       


        mFitMindDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialogDetails("fitmind", "home_screen_details");

            }
        });

        mFitMindPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialogDetails("fitmind", "programs");
            }
        });



        return mView;

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

    private void meditationClick() {
        mMeditation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MeditationActivity.class));
            }
        });
    }

    private void mindsetClick() {
        mMindset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MindsetActivity.class));
            }
        });
    }

    private void motivationClick() {

        mMotivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), UploadVideoActivity.class);
                intent.putExtra("video_category", "motivation");
                intent.putExtra("toolbar_title", "Motivation");
                startActivity(intent);

            }
        });

    }

    private void breathoutClick() {
        mBreathout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadVideoActivity.class);
                intent.putExtra("video_category", "breath_work");
                intent.putExtra("toolbar_title", "Breathwork");
                startActivity(intent);

            }
        });
    }

    private void widgetsInitailization() {
        mBreathout = mView.findViewById(R.id.fitmind_breathwork);
        mMotivation = mView.findViewById(R.id.fitmind_motivation);
        mMindset = mView.findViewById(R.id.fitmind_mindset);
        mMeditation = mView.findViewById(R.id.fitmind_meditation);
        mFitMindDetails = mView.findViewById(R.id.fitmind_details);
        mFitMindPrograms = mView.findViewById(R.id.fitmind_programs);


    }

}
