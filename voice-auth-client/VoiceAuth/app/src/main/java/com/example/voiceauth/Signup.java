package com.example.voiceauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
        SaveP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Signup.this,MainActivity.class);
                startActivity(i);
            }
        });

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
        EditText userName = findViewById(R.id.UserN);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user = ref.child("users");
        Query query = user.orderByChild("name").equalTo(userName.getText().toString());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                processData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error handling don here
            }
        });

    }

    private void processData(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.hasChildren()) {
            Intent intent = new Intent(Signup.this ,SignupVoice.class);
            EditText userName = findViewById(R.id.UserN);
            EditText email = findViewById(R.id.Email);
            intent.putExtra("name",userName.getText().toString());
            intent.putExtra("email",email.getText().toString());
            startActivityForResult(intent,REQ_CODE_ADD_ITEM);
        }else{
            Toast.makeText(this, "UserName exist", Toast.LENGTH_SHORT).show();
        }
    }
}
