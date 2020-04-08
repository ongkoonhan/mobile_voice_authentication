package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class AuthPage extends AppCompatActivity {
    private Button Go, VoiceA;
    private final int REQ_CODE_ADD_ITEM =6789;
    private TextView TV14,TV15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_page);

        //Textviews 14 and 15 will appear after username is confirmed
        TV14 = (TextView) findViewById(R.id.TV14);
        TV15 = (TextView) findViewById(R.id.TV15);

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
                OpenFinal();
            }
        });


    }

    public void OpenFinal() {
        //for now, it will go to the final page
        //the actual code will be for the voice spoken to match with the sample collected
        Intent intent = new Intent(this,Final.class);
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
    }


    public boolean apiCall(){
        Boolean result = false;
        String requestURL = "YOUR_URL";
        String file_path = "";

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


}
