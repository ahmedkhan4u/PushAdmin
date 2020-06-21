package com.softrasol.zaid.pushadmin.Helper;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softrasol.zaid.pushadmin.Model.PointsModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadMindSetData {

    static boolean status = false;

    public static boolean uploadMindSetData(final String title, String description, String documentName
    , final List<PointsModel> list){



        final CollectionReference collectionReference =
                FirebaseFirestore.getInstance().collection("mindset");

        final DocumentReference documentReference = collectionReference.document(documentName);

        Map map = new HashMap();
        map.put("title", title);
        map.put("description", description);

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

        return status;
    }

}
