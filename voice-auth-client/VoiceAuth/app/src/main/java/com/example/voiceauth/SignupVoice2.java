package com.example.voiceauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class SignupVoice2 extends AppCompatActivity {
    MediaPlayer mediaPlayer ;
    String name ="";
    String email ="";
    public String FIREBASE_USERNAME;
    public String FIREBASE_PASSWORD;
    public FirebaseAuth mAuth;
    private Button buttonPlayLastRecordAudio,buttonStopPlayingRecording,Com;
    String AudioSavePathInDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_voice2);
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth =FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);
        buttonPlayLastRecordAudio = (Button)findViewById(R.id.PlayB);
        buttonStopPlayingRecording = (Button)findViewById(R.id.StopP);
        buttonPlayLastRecordAudio.setVisibility(View.VISIBLE);
        buttonStopPlayingRecording.setVisibility(View.VISIBLE);
        buttonStopPlayingRecording.setEnabled(false);
        Intent i = getIntent();
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");
        AudioSavePathInDevice = i.getStringExtra("path");

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {


                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(SignupVoice2.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                buttonStopPlayingRecording.setEnabled(false);
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                filechecker(findViewById(android.R.id.content));
                callfireBase();
            }
        });

        Com = (Button) findViewById(R.id.Com);
        Com.setVisibility(View.VISIBLE);
        Com.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                Intent i = new Intent(SignupVoice2.this,SignupVoice.class);
                i.putExtra("name",name);
                i.putExtra("email",email);
                startActivity(i);
            }
        });

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });
    }

    public void ReturnMain() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        Toast.makeText(this,"You are now a registered user",Toast.LENGTH_LONG).show();
    }

    public void callfireBase(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        User user = new User(name,email);
        ref.push().setValue(user);
    }

    public void filechecker(View v){
        File dir = getFilesDir();
        File from = new File(AudioSavePathInDevice);
        convertMP3toWAV(from,v);

    }

    public void convertMP3toWAV(File mp3,View v){
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                File to = new File(getFilesDir()+"/"+name+".wav");
                convertedFile.renameTo(to);
                AudioSavePathInDevice =getFilesDir()+"/"+name+".wav";
                File fdelete = new File(getFilesDir()+"/temp.mp3");
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted : "+fdelete.toString());
                    } else {
                        System.out.println("file not Deleted");
                    }
                }
                Toast.makeText(v.getContext(), "SUCCESS: " + to.getPath(), Toast.LENGTH_LONG).show();
                uploadFirebase();
                ReturnMain();
            }
            @Override
            public void onFailure(Exception error) {
                Toast.makeText(v.getContext(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.v("Error", error.getMessage().toString());
            }
        };
        Toast.makeText(v.getContext(), "Converting audio file...", Toast.LENGTH_SHORT).show();
        AndroidAudioConverter.with(v.getContext())
                .setFile(mp3)
                .setFormat(AudioFormat.WAV)
                .setCallback(callback)
                .convert();
    }

    public void uploadFirebase() {
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth =FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://projectonly-b6dda.appspot.com");
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("user/"+name+".wav");
        ProgressDialog progressDialog
                = new ProgressDialog(SignupVoice2.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        fileRef.putFile(Uri.fromFile(new File(AudioSavePathInDevice))).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(
                            UploadTask.TaskSnapshot taskSnapshot)
                    {

                        // Image uploaded successfully
                        // Dismiss dialog
                        progressDialog.dismiss();
                        Toast
                                .makeText(SignupVoice2.this,
                                        "Voice Uploaded!!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        File dir = getFilesDir();
                        if(dir.exists()){
                            File[] file = getFilesDir().listFiles();
                            for (File f : file) {
                                if (f.isFile() && f.getPath().substring(f.getPath().lastIndexOf('.'),f.getPath().length()).equals((".wav"))) {
                                    f.delete();
                                }
                            }
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(SignupVoice2.this,
                                        "Failed " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .addOnProgressListener(
                        new OnProgressListener<UploadTask.TaskSnapshot>() {

                            // Progress Listener for loading
                            // percentage on the dialog box
                            @Override
                            public void onProgress(
                                    UploadTask.TaskSnapshot taskSnapshot)
                            {
                                double progress
                                        = (100.0
                                        * taskSnapshot.getBytesTransferred()
                                        / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage(
                                        "Uploaded "
                                                + (int)progress + "%");
                            }
                            });

    }
}
