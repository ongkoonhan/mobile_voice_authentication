package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AuthPage extends AppCompatActivity {
    private Button Go, VoiceA;
    private final int REQ_CODE_ADD_ITEM =6789;
    public static final int RequestPermissionCode = 1;
    private TextView TV14,TV15;
    MediaRecorder mediaRecorder ;
    String AudioSavePathInDevice = null;
    public String FIREBASE_USERNAME;
    public String FIREBASE_PASSWORD;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_page);
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth =FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);

        //Textviews 14 and 15 will appear after username is confirmed
        TV14 = (TextView) findViewById(R.id.TV14);
        TV15 = (TextView) findViewById(R.id.TV15);
        TV15.setText(getResources().getString(R.string.Auth));
        //VoiceA button visibility default off, it will appear if the user name is registered
        //VoiceA button is use for voice authenication i.e. press and speak

        Go = (Button) findViewById(R.id.Go);
        VoiceA = (Button) findViewById(R.id.VoiceA);

        Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to say if username not found, put toast to say please register
                //If username is found, the VoiceA button will appear
                TV14.setVisibility(View.VISIBLE);
                TV15.setVisibility(View.VISIBLE);
                VoiceA.setVisibility(View.VISIBLE);
            }
        });

        VoiceA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()) {

                    AudioSavePathInDevice =
                            getFilesDir() + "/temp.wav";

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
                }
            }
        });


    }

    public void OpenFinal() {
        //for now, it will go to the final page
        //the actual code will be for the voice spoken to match with the sample collected
        apiCall();
        Intent intent = new Intent(this,Final.class);
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
    }


    public boolean apiCall(){
        Boolean result = false;
        String requestURL = "https://c2927d55.ngrok.io";
        String file_path = AudioSavePathInDevice;
        try {
            MultipartUtilityV2 multipart = new MultipartUtilityV2(requestURL);
            multipart.addFilePart("file_param_1", new File(file_path));
            multipart.addFilePart("file_param_1", new File(file_path));
            String response = multipart.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
                    Toast.makeText(AuthPage.this, "Recording stops. Limit reached", Toast.LENGTH_LONG).show();
                    mr.stop();
                    OpenFinal();
                }
            }
        });


    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(AuthPage.this, new
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
                        Toast.makeText(AuthPage.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AuthPage.this,"Permission Denied",Toast.LENGTH_LONG).show();
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


}
