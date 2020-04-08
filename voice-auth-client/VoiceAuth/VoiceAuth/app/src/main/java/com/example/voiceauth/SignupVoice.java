package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SignupVoice extends AppCompatActivity {
    private Button buttonStart,buttonStop,buttonPlayLastRecordAudio,buttonStopPlayingRecording,Com;
    private final int REQ_CODE_ADD_ITEM =6789;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    String name ="";
    String email ="";
    public String FIREBASE_USERNAME;
    public String FIREBASE_PASSWORD;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_voice);
        mAuth = FirebaseAuth.getInstance();
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);
        //Need to Yicheng's part on voice recording
        buttonStart =  (Button)findViewById(R.id.Record);
        buttonStop = (Button)findViewById(R.id.Stop);
        buttonPlayLastRecordAudio = (Button)findViewById(R.id.PlayB);
        buttonStopPlayingRecording = (Button)findViewById(R.id.StopP);
        TextView contentToSay =findViewById(R.id.TV7);
        contentToSay.setText(getResources().getString(R.string.Regi));
        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);
        Intent i = getIntent();
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File fdelete = new File(getFilesDir()+"/temp.mp3");
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted : "+fdelete.toString());
                    } else {
                        System.out.println("file not Deleted");
                    }
                }
                if(checkPermission()) {

                    AudioSavePathInDevice =
                            getFilesDir() + "/temp.mp3";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(SignupVoice.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);

                Toast.makeText(SignupVoice.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(SignupVoice.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
                filechecker();
                callfireBase();
            }
        });

        //Button to complete registration once sample is taken and ok
        Com = (Button) findViewById(R.id.Com);
        Com.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Need to set a command to say if there is no voice sample detected, the button will show a toast "Please record sample"
                File dir = getFilesDir();
                if(dir.exists()){
                    File[] file = getFilesDir().listFiles();
                    for (File f : file) {
                        if (f.isFile() && f.getPath().substring(f.getPath().lastIndexOf('.'),f.getPath().length()).equals((".wav"))) {
                            File delete = new File(getFilesDir()+name+".wave");
                            delete.delete();
                            ReturnMain();
                        }
                    }
                }
            }
        });
    }

    public void ReturnMain() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
        Toast.makeText(this,"You are now a registered user",Toast.LENGTH_LONG).show();
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(22050);
        mediaRecorder.setMaxDuration(7000); // 1000 = 1 sec
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    Toast.makeText(SignupVoice.this, "Recording stops. Limit reached", Toast.LENGTH_LONG).show();
                    mr.stop();
                    buttonStop.setEnabled(false);
                    buttonStart.setEnabled(true);
                    buttonStopPlayingRecording.setEnabled(false);
                    buttonPlayLastRecordAudio.setEnabled(true);
                    filechecker();
                }
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(SignupVoice.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(SignupVoice.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupVoice.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void callfireBase(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        User user = new User(name,email);
        ref.push().setValue(user);
    }

    public void filechecker(){
        File dir = getFilesDir();
        if(dir.exists()){
            File from = new File(dir,"temp.mp3");
            File to = new File(dir,name+".wav");
            if(from.exists())
                from.renameTo(to);
        }

        File fdelete = new File(getFilesDir()+"/temp.mp3");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted : "+fdelete.toString());
            } else {
                System.out.println("file not Deleted");
            }
        }
    }
}
