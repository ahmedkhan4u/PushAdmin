package com.softrasol.zaid.pushadmin.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.softrasol.zaid.pushadmin.MotivationActivity;
import com.softrasol.zaid.pushadmin.R;
import com.softrasol.zaid.pushadmin.UploadBreathOutDataActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class FitMindFragment extends Fragment {


    public FitMindFragment() {
        // Required empty public constructor
    }

    private View mView;
    private RelativeLayout mBreathout, mMotivation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_fit_mind, container, false);

        widgetsInitailization();

        breathoutClick();
        motivationClick();

        return mView;
    }

    private void motivationClick() {

        mMotivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MotivationActivity.class));
            }
        });

    }

    private void breathoutClick() {
        mBreathout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), UploadBreathOutDataActivity.class));
            }
        });
    }

    private void widgetsInitailization() {
        mBreathout = mView.findViewById(R.id.fitmind_breathwork);
        mMotivation = mView.findViewById(R.id.fitmind_motivation);
    }

}
