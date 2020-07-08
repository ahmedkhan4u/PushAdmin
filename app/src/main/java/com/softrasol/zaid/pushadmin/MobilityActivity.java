package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.softrasol.zaid.pushadmin.Adapters.PointsAdapter;
import com.softrasol.zaid.pushadmin.Helper.UploadVideoData;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class MobilityActivity extends AppCompatActivity {

    VideoView mBreathOutVideo;
    private Uri videoUri, imageUri;
    private RecyclerView mRecyclerView;
    private List<PointsModel> list = new ArrayList<>();
    private TextInputEditText mTxtTitle;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobility);

        widgetsInitailization();
        getDataFromFirebaseDatabase();

    }

    private void getDataFromFirebaseDatabase() {

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("videos");
        final DocumentReference documentReference = collectionReference
                .document("challenges");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){

                        if (task.getResult().contains("title")){
                            title = task.getResult().getString("title");
                            mTxtTitle.setText(title);
                        }

                        if (task.getResult().contains("video_url")){
                            videoUri = Uri.parse(task.getResult().getString("video_url"));
                            mBreathOutVideo.setVideoURI(videoUri);
                            mBreathOutVideo.start();
                            MediaController mediaController = new MediaController(MobilityActivity.this);
                            mBreathOutVideo.setMediaController(mediaController);
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

                                            PointsModel model = snapshot.toObject(PointsModel.class);

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


    private void pickVideoFromGallary() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),1);

    }

    private void widgetsInitailization() {
        mBreathOutVideo = findViewById(R.id.breathout_video);
        mRecyclerView = findViewById(R.id.recyclerview_breathwork);
        mTxtTitle = findViewById(R.id.txt_breathwork_title);
    }

    public void BackClick(View view) {
        onBackPressed();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                videoUri = data.getData();
                Uri uri = Uri.parse(videoUri+"");
                mBreathOutVideo.setVideoURI(uri);
                mBreathOutVideo.start();
                MediaController mediaController = new MediaController(MobilityActivity.this);
                mBreathOutVideo.setMediaController(mediaController);

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Toast.makeText(this, "Image Choosed", Toast.LENGTH_SHORT).show();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void ChooseVideoClick(View view) {

        pickVideoFromGallary();

    }

    public void AddNoteClick(View view) {

        showBottomSheetDialog();

    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog dialog = new BottomSheetDialog(MobilityActivity.this);
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

                list.add(new PointsModel(title, description));
                dialog.cancel();

                showPointsOnRecyclerView();

            }
        });

    }

    private void showPointsOnRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        PointsAdapter adapter = new PointsAdapter(MobilityActivity.this, list);
        mRecyclerView.setAdapter(adapter);

    }

    public void UploadDataClick(View view) {

        title = mTxtTitle.getText().toString().trim();

        if (title.length()<3){
            mTxtTitle.setError("Title too short");
            mTxtTitle.requestFocus();
            return;
        }

        if (videoUri == null){
            Toast.makeText(this, "Kindly choose video", Toast.LENGTH_SHORT).show();
            return;
        }

        if (list.isEmpty()){
            Toast.makeText(this, "Kindly add a point", Toast.LENGTH_SHORT).show();
            return;
        }


        boolean result = UploadVideoData.uploadVideoData("mobility","mobility",
                videoUri+"", title, list);

        if (result = true){
            Toast.makeText(this, "Data Uploaded", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(this, "Failure Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    public void ChooseBgImage(View view) {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,2)
                .start(MobilityActivity.this);
    }
}
