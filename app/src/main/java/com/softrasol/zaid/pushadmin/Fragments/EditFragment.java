package com.softrasol.zaid.pushadmin.Fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softrasol.zaid.pushadmin.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment {


    public EditFragment() {
        // Required empty public constructor
    }

    private View mView;
    private Button mBtnEditImage, mBtnEdit;
    private TextInputEditText mTxtWatermark;
    private ImageView mImgWatermark;
    private Uri imageUri;
    private StorageReference mStorageReference;
    private String downloadUrl;
    private ProgressDialog progressDialog;
    private CollectionReference collectionReference;
    private DocumentReference documentReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        collectionReference = FirebaseFirestore.getInstance().collection("Watermarks");
        documentReference = collectionReference.document("1");
        progressDialog = new ProgressDialog(getContext());
        widgetsInflation();
        getDataFromFirebaseShow();
        editImageClick();
        editWatermarkTextClick();

        return mView;
    }

    private void editWatermarkTextClick() {

        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setMessage("Are you sure you want to edit watermark");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                updateWaterMark();

                                dialog.cancel();
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
            }
        });

    }

    private void updateWaterMark() {

        String waterMarkText = mTxtWatermark.getText().toString().trim();
        final Map map = new HashMap();
        map.put("textwatermark", waterMarkText);

        documentReference.update(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Watermark Updated Successfully", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }else {
                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                progressDialog.cancel();
                                Toast.makeText(getContext(), "Watermark Updated Successfully", Toast.LENGTH_LONG).show();
                            }else {
                                progressDialog.cancel();
                                Toast.makeText(getContext(), "Database Error : "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }

    private void editImageClick() {
        mBtnEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(getContext(), EditFragment.this);
            }
        });
    }

    private void getDataFromFirebaseShow() {

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()){
                    if (task.getResult().contains("image_url")){
                        if (task.isSuccessful()){
                            String imageUrl = task.getResult().get("image_url").toString();
                            Picasso.get().load(imageUrl)
                                    .resize(400,400)
                                    .placeholder(R.drawable.img_temp).
                                    into(mImgWatermark);
                        }
                    }

                    if (task.getResult().contains("textwatermark")){
                        if (task.isSuccessful()){
                            String waterMarkText = task.getResult().get("textwatermark").toString();
                            mTxtWatermark.setText(waterMarkText);
                        }
                    }
                }
            }
        });

    }

    private void widgetsInflation() {

        mBtnEditImage = mView.findViewById(R.id.btn_choose_image_edit);
        mBtnEdit = mView.findViewById(R.id.btn_edit_text);
        mTxtWatermark = mView.findViewById(R.id.txt_watermark_edit);
        mImgWatermark = mView.findViewById(R.id.img_watermark_edit);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mImgWatermark.setImageURI(imageUri);
                
                uploadImageToFirebaseStorage();
                
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {

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

                    saveDataToFirestoreDatabase(downloadUrl);

                } else {
                    // Handle failures
                    // ...
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void saveDataToFirestoreDatabase(String downloadUrl) {

        final Map map = new HashMap();
        map.put("image_url", downloadUrl);

        documentReference.update(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Image Updated Successfully", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }else {
                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                progressDialog.cancel();
                                Toast.makeText(getContext(), "Image Updated Successfully", Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(getContext(), "Database Error : "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                progressDialog.cancel();
                            }
                        }
                    });
                }
            }
        });

    }

}
