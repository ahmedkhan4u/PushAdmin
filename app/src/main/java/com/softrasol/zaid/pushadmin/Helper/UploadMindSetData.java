package com.softrasol.zaid.pushadmin.Helper;

import android.net.Uri;

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
import java.util.List;
import java.util.Map;

public class UploadMindSetData {

    static boolean status = false;
    static String downloadUrl;

    public static boolean uploadMindSetData(final String title, final String description, String documentName
    , final List<PointsModel> list, String imageUri){



        final CollectionReference collectionReference =
                FirebaseFirestore.getInstance().collection("mindset");

        final DocumentReference documentReference = collectionReference.document(documentName);


        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Images");
        final StorageReference ref = storageRef.child("mindset");

        UploadTask uploadTask = ref.putFile(Uri.parse(imageUri));

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


                    Map map = new HashMap();
                    map.put("title", title);
                    map.put("description", description);
                    map.put("image_url", downloadUrl);

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){

                                for (int i=0; i<list.size(); i++){

                                    PointsModel model = new PointsModel(list.get(i).getTitle(),
                                            list.get(i).getSub_title());

                                    CollectionReference collectionReference1 =
                                            documentReference.collection("points_data");

                                    DocumentReference documentReference1 =
                                            collectionReference1.document(i+"");

                                    documentReference1.set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                status = true;
                                            }else {
                                                status = false;
                                            }
                                        }
                                    });

                                }

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
