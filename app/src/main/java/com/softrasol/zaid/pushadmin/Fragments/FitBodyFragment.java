package com.softrasol.zaid.pushadmin.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.softrasol.zaid.pushadmin.ChallengesActivity;
import com.softrasol.zaid.pushadmin.ExerciseActivity;
import com.softrasol.zaid.pushadmin.MobilityActivity;
import com.softrasol.zaid.pushadmin.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FitBodyFragment extends Fragment {


    public FitBodyFragment() {
        // Required empty public constructor
    }

    private View mView;
    private RelativeLayout mChallenges, mExercise, mMobility;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_fit_body, container, false);

        widgetsInitailization();
        mobilityClick();
        challengesClick();
        exerciseClick();

        return mView;
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
    }

}
