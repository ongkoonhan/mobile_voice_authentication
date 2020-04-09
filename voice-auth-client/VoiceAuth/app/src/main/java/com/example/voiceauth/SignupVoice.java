package com.example.voiceauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SignupVoice extends AppCompatActivity {
    private Button buttonStart,buttonStop;
    private final int REQ_CODE_ADD_ITEM =6789;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    public static final int RequestPermissionCode = 1;
    String name ="";
    String email ="";
    private int seconds = 7;
    private CountDownTimer mCountDownTimer;
    private static  final long START_TIME_IN_MILLIS = 7000;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_voice);
        timer = findViewById(R.id.timer);
        //Need to Yicheng's part on voice recording
        buttonStart =  (Button)findViewById(R.id.Record);
        buttonStop = (Button)findViewById(R.id.Stop);
        TextView contentToSay =findViewById(R.id.TV7);
        contentToSay.setText(getResources().getString(R.string.Regi));
        buttonStop.setEnabled(false);
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
                        startTimer();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                Toast.makeText(SignupVoice.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
                nextPage();
            }
        });


        //Button to complete registration once sample is taken and ok

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
                    nextPage();
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

    public void nextPage(){
        Intent i = new Intent(SignupVoice.this,SignupVoice2.class);
        i.putExtra("name",name);
        i.putExtra("email",email);
        i.putExtra("path",AudioSavePathInDevice);
        startActivity(i);
    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private void updateCountDownText(){
        int minutes = (int) (mTimeLeftInMillis/1000)/60;
        int seconds = (int) (mTimeLeftInMillis/1000)%60;
        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        timer.setText(timeLeftFormatted.toString());
    }

}
