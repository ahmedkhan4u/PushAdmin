package com.softrasol.zaid.pushadmin.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softrasol.zaid.pushadmin.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadsFragment extends Fragment {


    public UploadsFragment() {
        // Required empty public constructor
    }

    private View mView;
    private Button mBtnChooseImage, mBtnSave;
    private TextInputEditText mTxtWatermark;
    private ImageView mImgWatermark;
    private Uri imageUri;
    private StorageReference mStorageReference;
    private String downloadUrl;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_uploads, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        progressDialog = new ProgressDialog(getContext());

        widgetsInflation();
        chooseImageClick();
        saveButtonClick();


        return mView;
    }

    private void saveButtonClick() {

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String watermarkText = mTxtWatermark.getText().toString().trim();

                if (watermarkText.isEmpty()){
                    mTxtWatermark.setError("Required");
                    mTxtWatermark.requestFocus();
                    return;
                }

                if (watermarkText.length() < 3){
                    mTxtWatermark.setError("Too Short");
                    mTxtWatermark.requestFocus();
                    return;
                }

                if (imageUri == null){
                    Toast.makeText(getContext(), "Please Choose an image", Toast.LENGTH_LONG).show();
                    return;
                }


                uploadDataToFirebaseDatabase(imageUri, watermarkText);

            }
        });

    }

    private void uploadDataToFirebaseDatabase(Uri imageUri, final String watermarkText) {

        progressDialog.setTitle("Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mStorageReference = FirebaseStorage.getInstance().getReference().child("Images");

        final StorageReference ref = mStorageReference.child("img");
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
                    Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_LONG).show();

                    saveDataToFirestoreDatabase(downloadUrl, watermarkText);

                } else {
                    // Handle failures
                    // ...
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void saveDataToFirestoreDatabase(String downloadUrl, String watermarkText) {

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Watermarks");
        DocumentReference documentReference = collectionReference.document("1");

        Map map = new HashMap();
        map.put("image_url", downloadUrl);
        map.put("textwatermark", watermarkText);
        documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Water Mark Set", Toast.LENGTH_SHORT).show();
                    mTxtWatermark.setText("");
                    progressDialog.dismiss();
                }else {
                    Toast.makeText(getContext(), "Database Error : "+task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

    }

    private void chooseImageClick() {

        mBtnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(getContext(), UploadsFragment.this);

            }
        });

    }

    private void widgetsInflation() {

        mBtnChooseImage = mView.findViewById(R.id.btn_choose_image);
        mBtnSave = mView.findViewById(R.id.btn_save);
        mTxtWatermark = mView.findViewById(R.id.txt_watermark);
        mImgWatermark = mView.findViewById(R.id.img_watermark);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mImgWatermark.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
