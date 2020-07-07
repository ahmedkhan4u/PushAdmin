package com.softrasol.zaid.pushadmin.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softrasol.zaid.pushadmin.R;
import com.softrasol.zaid.pushadmin.UploadUserNameImageBg;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditUserNameBgFragment extends Fragment {


    public EditUserNameBgFragment() {
        // Required empty public constructor
    }

    private View mView;
    private ImageButton mBtnBack;
    private Button mBtnChooseImage;
    private Button mBtnUploadImage;

    ImageView layout;
    private Uri imageUri;
    private String downloadUrl;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_edit_user_name_bg, container, false);

        mBtnBack = mView.findViewById(R.id.btn_back);
        mBtnChooseImage = mView.findViewById(R.id.btn_choose_image);
        mBtnUploadImage = mView.findViewById(R.id.btn_upload_image);
        layout = mView.findViewById(R.id.layout);

        dialog = new ProgressDialog(getActivity());

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });



        mBtnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(2,3)
                        .start(getContext(), EditUserNameBgFragment.this);
            }
        });

        mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null){
                    showToast("Kindly choose an image");
                    return;
                }

                uploadDataToFirebaseFireStore();
            }
        });

        return mView;
    }




    private void uploadDataToFirebaseFireStore() {

        dialog.setTitle("Wait...");
        dialog.show();
        dialog.setCancelable(false);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");


        final StorageReference ref = storageReference.child("username_bg");
        UploadTask uploadTask = ref.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult().toString();

                    CollectionReference collectionReference = FirebaseFirestore.getInstance()
                            .collection("name_bg_image");
                    DocumentReference documentReference = collectionReference
                            .document("name_bg_image");

                    Map map = new HashMap();
                    map.put("image_url", downloadUrl+"");

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                showToast("Data Uploaded");
                                dialog.cancel();
                            }else {
                                showToast(task.getException().getMessage());
                                dialog.cancel();
                            }
                        }
                    });




                } else {
                    // Handle failures
                    // ...
                    dialog.cancel();
                }
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                Toast.makeText(getActivity(), "Image Choosed", Toast.LENGTH_SHORT).show();
                layout.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }



}
