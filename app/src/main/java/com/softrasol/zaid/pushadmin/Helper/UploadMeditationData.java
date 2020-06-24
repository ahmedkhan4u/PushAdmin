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

    public static boolean uploadMeditationData(final String title, final String description, final String audioUri
    , String documentName){


        final CollectionReference collectionReference =
                FirebaseFirestore.getInstance().collection("meditation");

        final DocumentReference documentReference = collectionReference.document(documentName);



        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("audio");

        final StorageReference ref = storageRef.child("meditation");
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

                    Map map = new HashMap();
                    map.put("title", title);
                    map.put("description", description);
                    map.put("audio_url", audioUri);

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                status = true;
                            }else {
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
