package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.softrasol.zaid.pushadmin.Adapters.TabsAccessorAdapter;
import com.softrasol.zaid.pushadmin.Fragments.EditFragment;
import com.softrasol.zaid.pushadmin.Fragments.EditUserNameBgFragment;
import com.softrasol.zaid.pushadmin.Fragments.FitBodyFragment;
import com.softrasol.zaid.pushadmin.Fragments.FitMindFragment;
import com.softrasol.zaid.pushadmin.Fragments.UploadsFragment;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);

        tabLayoutImplementation();
        loginSyncedAdmin();


    }

    private void loginSyncedAdmin() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword("admin@push.com", "123456")
    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

            }
        });
    }

    private void tabLayoutImplementation() {

        TabsAccessorAdapter adapter = new TabsAccessorAdapter(getSupportFragmentManager());

        //adapter.setFragment(new UploadsFragment(),"Upload");
        adapter.setFragment(new EditFragment(), "Water Mark");
        adapter.setFragment(new FitMindFragment(), "Fit Mind");
        adapter.setFragment(new FitBodyFragment(), "Fit Body");
        adapter.setFragment(new EditUserNameBgFragment(), "Edit User Bg");

        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(adapter);

    }
}
