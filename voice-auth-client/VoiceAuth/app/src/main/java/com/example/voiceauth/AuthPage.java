package com.example.voiceauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AuthPage extends AppCompatActivity {
    private Button Go, VoiceA;
    private final int REQ_CODE_ADD_ITEM =6789;
    private TextView TV14,TV15;
    MediaRecorder mediaRecorder ;
    String AudioSavePathInDevice = null;
    public String FIREBASE_USERNAME;
    public String FIREBASE_PASSWORD;
    public FirebaseAuth mAuth;
    public String name = "pos_sample";
    public static final int RequestPermissionCode = 1;


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
                EditText userName = findViewById(R.id.RegN);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                DatabaseReference user = ref.child("users");
                name = userName.getText().toString();
                Query query = user.orderByChild("name").equalTo(name);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        AuthprocessData(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //error handling don here
                    }
                });
            }
        });

        VoiceA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    Toast.makeText(AuthPage.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }
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
                    filechecker(findViewById(android.R.id.content));
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

    public void filechecker(View v){
        File dir = getFilesDir();
        File from = new File(getFilesDir()+"/temp.mp3");
        convertMP3toWAV(from,v);
    }

    public void convertMP3toWAV(File mp3,View v){
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                File to = new File(getFilesDir()+"/"+name+"1.wav");
                convertedFile.renameTo(to);
                download();
                Toast.makeText(v.getContext(), "SUCCESS: " + to.getPath(), Toast.LENGTH_LONG).show();
                File fdelete = new File(getFilesDir()+"/temp.mp3");
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Toast.makeText(AuthPage.this, "file Deleted :"+fdelete.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("file not Deleted");
                    }
                }
            }
            @Override
            public void onFailure(Exception error) {
                Toast.makeText(v.getContext(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        Toast.makeText(v.getContext(), "Converting audio file...", Toast.LENGTH_SHORT).show();
        AndroidAudioConverter.with(v.getContext())
                .setFile(mp3)
                .setFormat(AudioFormat.WAV)
                .setCallback(callback)
                .convert();

    }

    public List<String> apiCall(){
        Boolean result = false;

        String requestURL = "https://cs461voiceauth.burrow.io/verify";
        String file_path = getFilesDir()+"/"+name+"1.wav";
        List<String> response = new ArrayList<String>();
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, "UTF-8");
            multipart.addFilePart("wav1", new File(file_path));
            String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+name+".wav";
            multipart.addFilePart("wav2", new File(path));
            response = multipart.finish();
            new File(file_path).delete();
            new File(path).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void AuthprocessData(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.hasChildren()) {
            Toast.makeText(this, "No Such UserName", Toast.LENGTH_SHORT).show();
        }else{
            //If username is found, the VoiceA button will appear
            TV14.setVisibility(View.VISIBLE);
            TV15.setVisibility(View.VISIBLE);
            VoiceA.setVisibility(View.VISIBLE);
        }
    }

    public void download(){
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth =FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("user/"+name+".wav");
        File localFile = new File(getFilesDir()+"/"+name+".wav");
        String path =localFile.getPath();
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                downloadFile(AuthPage.this,name,".wav", Environment.DIRECTORY_DOWNLOADS+"/".toString(),url);
                List<String>result = apiCall();
                if(result.isEmpty()){
                    Toast.makeText(AuthPage.this,"Error In Moving",Toast.LENGTH_SHORT).show();
                }else{
                    if(result.size()<=5){
                        if(result.get(2).contains("true")){
                            Toast.makeText(AuthPage.this,"Verify",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AuthPage.this,Final.class);
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST",(Serializable)result);
                            intent.putExtra("BUNDLE",args);

                            startActivityForResult(intent,REQ_CODE_ADD_ITEM);
                        }else{
                            Toast.makeText(AuthPage.this,"Not Verify",Toast.LENGTH_SHORT).show();
                            TextView tv = findViewById(R.id.Verifylog);
                            tv.setText(result.toString());
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalFilesDir(context,destinationDirectory,fileName+fileExtension);
        downloadmanager.enqueue(request);

    }


}
