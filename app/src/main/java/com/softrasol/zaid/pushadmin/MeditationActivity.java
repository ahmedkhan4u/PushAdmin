package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.softrasol.zaid.pushadmin.Helper.UploadMeditationData;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MeditationActivity extends AppCompatActivity {

    private Uri audioUri;
    private MediaPlayer mediaPlayer;
    boolean status = false;
    private TextInputEditText mTxtTitle, mTxtDescription;
    private String title, description;
    private Spinner mSpinner;
    private String category;
    private Uri imageUri;

    private String downloadAudioUrl, downloadImageUrl;

    private ProgressDialog progressDialog;

    private String []list = {"Choose Category","Wise", "Happiness", "Health"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        mTxtTitle = findViewById(R.id.txt_meditation_title);
        mTxtDescription = findViewById(R.id.txt_meditation_description);
        mSpinner = findViewById(R.id.spinner_meditation);
        mediaPlayer = new MediaPlayer();

        progressDialog = new ProgressDialog(this);

        implementSpinner();

    }

    private void getDataFromFirebaseDatabase(String cat) {

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("meditation");
        final DocumentReference documentReference = collectionReference
                .document(category);

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
                                downloadImageUrl = task.getResult().getString("image_url");


                            }catch (Exception ex){
                            }

                        }

                        if (task.getResult().contains("audio_url")){
                            audioUri = Uri.parse(task.getResult().getString("audio_url"));
                            downloadAudioUrl = task.getResult().getString("audio_url");
                        }

                        if (task.getResult().contains("category")){
                            category = task.getResult().getString("category");
                        }


                        if (task.getResult().contains("description")){
                            description = task.getResult().getString("description");
                            mTxtDescription.setText(description);
                        }

                    }
                }
            }
        });

    }


    private void implementSpinner() {

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                list);

        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = list[position];

                getDataFromFirebaseDatabase(category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void SaveMeditationData(View view) {

        showProgressDialog();

        title = mTxtTitle.getText().toString().trim();
        description = mTxtDescription.getText().toString().trim();



        if (category.equalsIgnoreCase("Choose Category")){
            Toast.makeText(this, "Kindly choose a category", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("meditation");

        final DocumentReference documentReference = collectionReference.document(category);

        Map map = new HashMap();
        map.put("audio_url", downloadAudioUrl+"");
        map.put("title", title);
        map.put("description",description);
        map.put("category", category);
        map.put("image_url", downloadImageUrl+"");

        documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
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

    public void BackClick(View view) {
        onBackPressed();
        mediaPlayer.stop();
    }

    public void ChooseAudioClick(View view) {
        pickAudioFromGallary();
    }

    private void pickAudioFromGallary() {


        if (category.equalsIgnoreCase("Choose Category")){
            showMessage("Kindly Choose Category First");
            return;
        }

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Audio"),1);


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                audioUri = data.getData();
                Toast.makeText(this, "Audio Choosed", Toast.LENGTH_SHORT).show();
                uploadAudioToFirebaseFirestore(audioUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Toast.makeText(this, "Image Choosed", Toast.LENGTH_SHORT).show();
                uploadImageToFirebaseFirestore(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void PlayAudioClick(View view) {

        if (audioUri != null){
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(MeditationActivity.this, audioUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (status == true){
                status = false;
                mediaPlayer.stop();
            }

            if (status == false){
                status = true;
                mediaPlayer.start();
            }


        }

    }

    public void ChooseBgImage(View view) {

        if (category.equalsIgnoreCase("Choose Category")){
            showMessage("Kindly Choose Category First");
            return;
        }

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2,3)
                .start(MeditationActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
    }

    private void uploadAudioToFirebaseFirestore(final Uri uri) {

        if (category.equalsIgnoreCase("Choose Category")){
            return;
        }

        showProgressDialog();

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("audio");
        final StorageReference ref = storageReference.child("meditation_"+category);
        UploadTask uploadTask = ref.putFile(uri);

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("meditation");

        final DocumentReference documentReference = collectionReference.document(category);

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
                    downloadAudioUrl = task.getResult().toString();


                    Map map = new HashMap();
                    map.put("audio_url", downloadAudioUrl+"");
                    map.put("title", title);
                    map.put("description",description);
                    map.put("category", category);
                    map.put("image_url", downloadImageUrl+"");

                    documentReference.set(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                showMessage("Audio Uploaded");
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

    private void uploadImageToFirebaseFirestore(final Uri uri) {


        showProgressDialog();

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("Images");
        final StorageReference ref = storageReference.child("meditation_"+category);
        UploadTask uploadTask = ref.putFile(uri);

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("meditation");

        final DocumentReference documentReference = collectionReference.document(category);

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
                    downloadImageUrl = task.getResult().toString();


                    Map map = new HashMap();
                    map.put("audio_url", downloadAudioUrl+"");
                    map.put("title", title);
                    map.put("description",description);
                    map.put("category", category);
                    map.put("image_url", downloadImageUrl+"");

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
