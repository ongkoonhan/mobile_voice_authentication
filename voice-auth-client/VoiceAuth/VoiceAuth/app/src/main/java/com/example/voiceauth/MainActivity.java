package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button Login, SignUp;
    private final int REQ_CODE_ADD_ITEM =6789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Login = (Button) findViewById(R.id.Login);
        SignUp = (Button) findViewById(R.id.SignUp);
        //login is to go to the authentication page
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAuth();
            }
        });
        //signup is to go to the page to register as an user
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenSignup();
            }
        });

    }
    //Intent to go to Authentication page
    public void OpenAuth() {
        Intent intent = new Intent(this,AuthPage.class);
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
    }
    //Intent to go to Singup page
    public void OpenSignup() {
        Intent intent = new Intent(this,Signup.class);
        startActivityForResult(intent,REQ_CODE_ADD_ITEM);
    }
}
