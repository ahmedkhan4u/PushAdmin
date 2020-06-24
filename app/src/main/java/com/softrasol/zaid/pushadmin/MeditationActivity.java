package com.softrasol.zaid.pushadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.softrasol.zaid.pushadmin.Helper.UploadMeditationData;

import java.io.IOException;

public class MeditationActivity extends AppCompatActivity {

    private Uri audioUri;
    private MediaPlayer mediaPlayer;
    boolean status = false;
    private TextInputEditText mTxtTitle, mTxtDescription;
    private String title, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        mTxtTitle = findViewById(R.id.txt_meditation_title);
        mTxtDescription = findViewById(R.id.txt_meditation_description);
        mediaPlayer = new MediaPlayer();



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


        boolean result = UploadMeditationData.uploadMeditationData(title, description,
                audioUri+"", "meditation");

        if (result = true){
            Toast.makeText(this, "Data Uploaded", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Failure Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    public void BackClick(View view) {
        onBackPressed();
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
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                audioUri = data.getData();
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
}
