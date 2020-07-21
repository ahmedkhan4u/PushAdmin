package com.softrasol.zaid.pushadmin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.softrasol.zaid.pushadmin.Helper.FileUtils;
import com.softrasol.zaid.pushadmin.Helper.UploadVideoData;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.vrgsoft.videcrop.VideoCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadVideoActivity extends AppCompatActivity {

    VideoView mBreathOutVideo;
    private Uri videoUri, imageUri;
    private RecyclerView mRecyclerView;
    private List<PointsModel> list = new ArrayList<>();
    private TextInputEditText mTxtTitle;
    private String title;
    private int CROP_REQUEST = 200;
    private String outputPath = "/storage/emulated/0/" + System.currentTimeMillis() + ".mp4";
    private String inputPath;

    private ImageView imgThumbnail;
    String video_category, toolbar_title;

    private Toolbar toolbar;
    private TextView toolbar_text;
    private String downloadUrl;

    private ProgressDialog progressDialog;


    CollectionReference collectionReference;
    DocumentReference documentReference;
    StorageReference storageReference;

    String thumbDownloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        progressDialog = new ProgressDialog(this);

        video_category = getIntent().getStringExtra("video_category");
        toolbar_title = getIntent().getStringExtra("toolbar_title");

        collectionReference = FirebaseFirestore.getInstance().collection("videos");
        documentReference = collectionReference.document(video_category);
        storageReference = FirebaseStorage.getInstance().getReference("videos");

        toolbarInitialization();
        widgetsInitailization();
        getDataFromFirebaseDatabase();


    }



    private void toolbarInitialization() {

        toolbar = findViewById(R.id.toolbar);
        toolbar_text = toolbar.findViewById(R.id.toolbar_text);

        toolbar_text.setText(toolbar_title);

    }

    public void getDataFromFirebaseDatabase() {

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("videos");
        final DocumentReference documentReference = collectionReference
                .document(video_category);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        if (task.getResult().contains("title")) {
                            title = task.getResult().getString("title");
                            mTxtTitle.setText(title);
                        }

                        if (task.getResult().contains("video_url")) {
                            videoUri = Uri.parse(task.getResult().getString("video_url"));
                            mBreathOutVideo.setVideoURI(videoUri);
                            mBreathOutVideo.start();
                            MediaController mediaController = new MediaController(UploadVideoActivity.this);
                            mBreathOutVideo.setMediaController(mediaController);
                        }

                        if (task.getResult().contains("thumbnailUrl")){
                            thumbDownloadUrl = task.getResult().getString("thumbnailUrl");
                            Picasso.get().load(thumbDownloadUrl)
                                    .into(imgThumbnail);
                        }


                        documentReference.collection("points_data").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (!list.isEmpty()) {
                                    list.clear();
                                }

                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {
                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                            PointsModel model = new PointsModel();
                                            model.setTitle(snapshot.getString("title"));
                                            model.setSub_title(snapshot.getString("sub_title"));
                                            model.setId(snapshot.getId());
                                            list.add(model);
                                        }
                                        showPointsOnRecyclerView();
                                    }
                                }
                            }

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
        startActivityForResult(Intent.createChooser(intent, "Select Video"), 1);

    }

    private void widgetsInitailization() {
        mBreathOutVideo = findViewById(R.id.video_view);
        mRecyclerView = findViewById(R.id.recyclerview);
        mTxtTitle = findViewById(R.id.txt_breathwork_title);
        imgThumbnail = findViewById(R.id.thumbnail);
    }

    public void BackClick(View view) {
        onBackPressed();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                videoUri = data.getData();
                Uri uri = Uri.parse(videoUri + "");
                mBreathOutVideo.setVideoURI(uri);
                mBreathOutVideo.start();
                MediaController mediaController = new MediaController(UploadVideoActivity.this);
                mBreathOutVideo.setMediaController(mediaController);
                inputPath = FileUtils.getPath(UploadVideoActivity.this, videoUri);
                //String path = getRealPathFromURI(videoUri);


                //Thumbnail

                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(inputPath,
                        MediaStore.Video.Thumbnails.MINI_KIND);



                Uri imageUri = getImageUri(getApplicationContext(), bitmap);

                imgThumbnail.setImageURI(imageUri);
                saveImageBitmapToStorage(imageUri);
                startActivityForResult(VideoCropActivity.createIntent(this, inputPath, outputPath), CROP_REQUEST);
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

        if (requestCode == CROP_REQUEST && resultCode == RESULT_OK) {
            File videoFile = new File(outputPath);
            videoUri = Uri.fromFile(videoFile);

            uploadVideoToFirebaseFirestore(videoUri);

//            videoUri = data.getData();
            Uri uri = Uri.parse(videoUri + "");
            mBreathOutVideo.setVideoURI(uri);
            mBreathOutVideo.start();
            imgThumbnail.setVisibility(View.GONE);
            MediaController mediaController = new MediaController(UploadVideoActivity.this);
            mBreathOutVideo.setMediaController(mediaController);
        }
    }

    private void saveImageBitmapToStorage(Uri imageUri) {


        final StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("thumbnail");

        final StorageReference ref = storageReference.child(video_category);

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
                    thumbDownloadUrl = task.getResult().toString();

                    final Map map = new HashMap();
                    map.put("thumbnailUrl", thumbDownloadUrl);

                } else {
                    // Handle failures
                    // ...
                }
            }
        });

    }

    private void uploadVideoToFirebaseFirestore(Uri videoUri) {

        showProgressDialog();

        final StorageReference ref = storageReference.child(video_category);
        UploadTask uploadTask = ref.putFile(videoUri);

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
                    map.put("video_url", downloadUrl);
                    map.put("title", title);
                    map.put("thumbnailUrl", thumbDownloadUrl);

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                showMessage("Video Uploaded");
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


    public void ChooseVideoClick(View view) {

        pickVideoFromGallary();

    }

    public void AddNoteClick(View view) {

        showBottomSheetDialog();

    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog dialog = new BottomSheetDialog(UploadVideoActivity.this);
        dialog.setContentView(R.layout.bottom_sheet_points_data);

        dialog.show();

        final TextInputEditText mTitle = dialog.findViewById(R.id.txt_point_title_bottom_sheet);
        final TextInputEditText mDescription = dialog.findViewById(R.id.txt_point_desc_bottom_sheet);

        Button mBtnAdd = dialog.findViewById(R.id.btn_add_bottom_sheet);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgressDialog();
                String title = mTitle.getText().toString().trim();
                String description = mDescription.getText().toString();

                if (title.isEmpty()) {
                    mTitle.setError("Required");
                    mTitle.requestFocus();
                    return;
                }

                if (description.isEmpty()) {
                    mDescription.setError("Required");
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
                        }else {
                            showMessage(task.getException().getMessage());
                            progressDialog.cancel();
                        }
                    }
                });



                dialog.cancel();

                showPointsOnRecyclerView();

            }
        });

    }

    private void showPointsOnRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        PointsAdapter adapter = new PointsAdapter(UploadVideoActivity.this, list,
                "videos" ,video_category);
        mRecyclerView.setAdapter(adapter);

    }

    public void UploadDataClick(View view) {

        showProgressDialog();
        title = mTxtTitle.getText().toString().trim();

        if (title.isEmpty()) {
            mTxtTitle.setError("Required");
            mTxtTitle.requestFocus();
            return;
        }

        Map map = new HashMap();
        map.put("title", title);
        map.put("video_url", videoUri+"");
        map.put("thumbnailUrl", thumbDownloadUrl);
        documentReference.update(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    showMessage("Title Saved");
                    progressDialog.cancel();
                }else {
                    showMessage(task.getException().getMessage());
                    progressDialog.cancel();
                }
            }
        });

    }

    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showProgressDialog(){
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Uploading data in progress.");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

}
