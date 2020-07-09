package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class UploadUserNameImageBg extends AppCompatActivity {

    ImageView layout;
    private Uri imageUri;
    private String downloadUrl;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_user_name_image_bg);

        layout = findViewById(R.id.layout);
        dialog = new ProgressDialog(this);


    }

    public void UploadImageClick(View view) {

        if (imageUri == null){
            showToast("Kindly choose an image");
            return;
        }

        uploadDataToFirebaseFireStore();


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
                                finish();
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

                Toast.makeText(this, "Image Choosed", Toast.LENGTH_SHORT).show();
                layout.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    public void ChooseImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2,3)
                .start(UploadUserNameImageBg.this);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void BackClick(View view) {
        onBackPressed();
    }

}
