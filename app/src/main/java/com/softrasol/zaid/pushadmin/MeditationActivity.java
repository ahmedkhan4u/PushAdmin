package com.softrasol.zaid.pushadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.softrasol.zaid.pushadmin.Helper.UploadMeditationData;
import com.softrasol.zaid.pushadmin.Model.PointsModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MeditationActivity extends AppCompatActivity {

    private Uri audioUri;
    private MediaPlayer mediaPlayer;
    boolean status = false;
    private TextInputEditText mTxtTitle, mTxtDescription;
    private String title, description;
    private Spinner mSpinner;
    private String category;
    private Uri imageUri;

    private String []list = {"Choose Category","Wise", "Happiness", "Health"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        mTxtTitle = findViewById(R.id.txt_meditation_title);
        mTxtDescription = findViewById(R.id.txt_meditation_description);
        mSpinner = findViewById(R.id.spinner_meditation);
        mediaPlayer = new MediaPlayer();

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

                            }catch (Exception ex){
                            }

                        }

                        if (task.getResult().contains("audio_url")){
                            audioUri = Uri.parse(task.getResult().getString("audio_url"));
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

        title = mTxtTitle.getText().toString().trim();
        description = mTxtDescription.getText().toString().trim();

        if (title.isEmpty()){
            mTxtTitle.setError("Title too short");
            mTxtTitle.requestFocus();
            return;
        }

        if (description.isEmpty()){
            mTxtDescription.setError("Description too short");
            mTxtDescription.requestFocus();
            return;
        }

        if (audioUri == null){
            Toast.makeText(this, "Kindly choose an audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equalsIgnoreCase("Choose Category")){
            Toast.makeText(this, "Kindly choose a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null){
            Toast.makeText(this, "Kindly choose background image", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean result = UploadMeditationData.uploadMeditationData(title, description,
                audioUri+"", category ,category, imageUri+"");

        if (result = true){
            Toast.makeText(this, "Data Uploaded", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(this, "Failure Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    public void BackClick(View view) {
        onBackPressed();
        mediaPlayer.stop();
    }

    public void ChooseAudioClick(View view) {
        pickAudioFromGallary();
    }

    private void pickAudioFromGallary() {

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
}
