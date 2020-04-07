package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {
    private Button SaveP,NextA;
    private final int REQ_CODE_ADD_ITEM =6789;
    public String FIREBASE_USERNAME;
    public String FIREBASE_PASSWORD;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        SaveP = (Button) findViewById(R.id.SaveP);
        NextA = (Button) findViewById(R.id.NextA);
        FIREBASE_USERNAME = getResources().getString(R.string.username);
        FIREBASE_PASSWORD = getResources().getString(R.string.password);
        mAuth =FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME,FIREBASE_PASSWORD);

        //go to another page to record voice
        NextA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //maybe need to add another code below to say that if nothing is saved, cannot go to next page
                //go to next signup page to input voice sample
                OpensignupV();
            }
        });
    }

    public void OpensignupV (){





        Intent intent = new Intent(this,SignupVoice.class);
        EditText userName = findViewById(R.id.UserN);
        EditText email = findViewById(R.id.Email);
        intent.putExtra("name",userName.getText().toString());
        intent.putExtra("email",email.getText().toString());
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
    }
}
