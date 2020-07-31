package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softrasol.zaid.pushadmin.Adapters.PointsAdapter;
import com.softrasol.zaid.pushadmin.Helper.UploadMindSetData;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<PointsModel> list = new ArrayList<>();
    private TextInputEditText mTxtTitle, mTxtDesription;
    private String title, description, downloadUrl;
    private ImageView bgImage;
    private Uri imageUri;

    private ProgressDialog progressDialog;


    CollectionReference collectionReference;
    DocumentReference documentReference;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        progressDialog = new ProgressDialog(this);

        collectionReference = FirebaseFirestore.getInstance()
                .collection("nutrition");
        documentReference = collectionReference
                .document("nutrition");
        storageReference = FirebaseStorage.getInstance()
                .getReference("audio");

        widgetsInitailization();
        getDataFromFirebaseDatabase();

        widgetsInitailization();
        getDataFromFirebaseDatabase();

    }

    private void getDataFromFirebaseDatabase() {

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("nutrition");
        final DocumentReference documentReference = collectionReference
                .document("nutrition");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){

                        if (task.getResult().contains("title")){
                            title = task.getResult().getString("title");
                            mTxtTitle.setText(title);
                        }

                        if (task.getResult().contains("image_url")){

                            try {

                                imageUri = Uri.parse(task.getResult().getString("image_url"));
                                Picasso.get().load(task.getResult().getString("image_url"))
                                        .into(bgImage);

                            }catch (Exception ex){
                            }

                        }

                        if (task.getResult().contains("description")){
                            description = task.getResult().getString("description");
                            mTxtDesription.setText(description);
                        }


                        documentReference.collection("points_data").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (!list.isEmpty()){
                                    list.clear();
                                }

                                if (task.isSuccessful()){
                                    if (task.getResult().size() > 0){
                                        for (QueryDocumentSnapshot snapshot : task.getResult()){

                                            PointsModel model = new PointsModel();
                                            model.setTitle(snapshot.getString("title"));
                                            model.setSub_title(snapshot.getString("sub_title"));
                                            model.setId(snapshot.getId());
                                            list.add(model);
                                        }
                                        showPointsOnRecyclerView();
                                    }
                                }                                        }

                        });



                    }
                }
            }
        });

    }

    private void widgetsInitailization() {
        mRecyclerView = findViewById(R.id.recyclerview_breathwork);
        mTxtTitle = findViewById(R.id.txt_breathwork_title);
        mTxtDesription = findViewById(R.id.txt_mindset_description);
        bgImage = findViewById(R.id.bg);
    }

    public void BackClick(View view) {
        onBackPressed();
    }

    public void AddNoteClick(View view) {

        showBottomSheetDialog();

    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog dialog = new BottomSheetDialog(NutritionActivity.this);
        dialog.setContentView(R.layout.bottom_sheet_points_data);

        dialog.show();

        final TextInputEditText mTitle = dialog.findViewById(R.id.txt_point_title_bottom_sheet);
        final TextInputEditText mDescription = dialog.findViewById(R.id.txt_point_desc_bottom_sheet);

        Button mBtnAdd = dialog.findViewById(R.id.btn_add_bottom_sheet);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = mTitle.getText().toString().trim();
                String description = mDescription.getText().toString();

                if (title.length()<3){
                    mTitle.setError("Title too short");
                    mTitle.requestFocus();
                    return;
                }

                if (description.length()<3){
                    mDescription.setError("Description too short");
                    mDescription.requestFocus();
                    return;
                }

                PointsModel model = new PointsModel(title, description);

                CollectionReference collectionReference1 = documentReference
                        .collection("points_data");

                collectionReference1.document().set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showMessage("Data Saved");
                            progressDialog.cancel();
                            getDataFromFirebaseDatabase();
                            dialog.cancel();
                        }else {
                            showMessage(task.getException().getMessage());
                            progressDialog.cancel();
                        }
                    }
                });


                showPointsOnRecyclerView();

            }
        });

    }

    private void showPointsOnRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        PointsAdapter adapter = new PointsAdapter(NutritionActivity.this, list,"nutrition","nutrition");
        mRecyclerView.setAdapter(adapter);

    }

    public void UploadDataClick(View view) {

        showProgressDialog();
        title = mTxtTitle.getText().toString().trim();
        description = mTxtDesription.getText().toString().trim();

        if (title.isEmpty()) {
            mTxtTitle.setError("Required");
            mTxtTitle.requestFocus();
            return;
        }

        Map map = new HashMap();
        map.put("title", title);
        map.put("image_url", imageUri+"");
        map.put("description", description);

        documentReference.update(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    showMessage("Data Saved");
                    progressDialog.cancel();
                }else {
                    showMessage(task.getException().getMessage());
                    progressDialog.cancel();
                }
            }
        });



    }

    public void ChooseBgImage(View view) {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(NutritionActivity.this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Toast.makeText(this, "Image Choosed", Toast.LENGTH_SHORT).show();
                bgImage.setImageURI(imageUri);
                uploadVideoToFirebaseFirestore(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void uploadVideoToFirebaseFirestore(Uri imageUri) {

        showProgressDialog();

        final StorageReference ref = storageReference.child("nutrition");
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


                    Map map = new HashMap();
                    map.put("image_url", downloadUrl);
                    map.put("title", mTxtTitle.getText().toString());
                    map.put("description",description);

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                showMessage("Image Uploaded");
                                progressDialog.cancel();
                            }else {
                                showMessage(task.getException().getMessage());
                                progressDialog.cancel();
                            }
                        }
                    });


                } else {
                    showMessage(task.getException().getMessage());
                    progressDialog.cancel();
                }
            }
        });

    }


    private void showProgressDialog(){
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Uploading data in progress.");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
