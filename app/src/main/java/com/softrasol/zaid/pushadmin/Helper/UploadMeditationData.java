package com.softrasol.zaid.pushadmin.Helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softrasol.zaid.pushadmin.Model.PointsModel;

import java.util.HashMap;
import java.util.Map;

public class UploadMeditationData {

    static boolean status = false;
    static String downloadUrl;
    static String imgaeDownloadUrl;



    public static boolean uploadMeditationData(final String title, final String description,
                                               final String audioUri
    , final String documentName, final String category, final String imageUri){


        final CollectionReference collectionReference =
                FirebaseFirestore.getInstance().collection("meditation");

        final DocumentReference documentReference = collectionReference.document(documentName);


        StorageReference imageStorage = FirebaseStorage.getInstance().getReference()
                .child("audio");


        final StorageReference ref = imageStorage.child("meditation_"+category);
        UploadTask uploadTask = ref.putFile(Uri.parse(audioUri));


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

                    final StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                            .child("Images");


                    final StorageReference ref2 = imageRef.child("meditation_"+category);
                    UploadTask uploadTask2 = ref2.putFile(Uri.parse(imageUri));

                    Task<Uri> urlTask = uploadTask2.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                imgaeDownloadUrl = task.getResult().toString();

                                Map map = new HashMap();
                                map.put("title", title);
                                map.put("description", description);
                                map.put("image_url", imgaeDownloadUrl);
                                map.put("audio_url", downloadUrl);
                                map.put("category", category);

                                documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            status = true;
                                        }else {
                                            status = false;
                                            Log.d("error", "Image Upload Error"+task.getException());
                                        }
                                    }
                                });


                            } else {
                                // Handle failures
                                // ...
                                status = false;
                            }
                        }
                    });



                } else {
                    // Handle failures
                    // ...
                    status = false;
                }
            }
        });


        return status;
    }


}
