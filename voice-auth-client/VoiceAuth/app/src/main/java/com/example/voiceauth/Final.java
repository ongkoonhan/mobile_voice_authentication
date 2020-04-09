package com.example.voiceauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Final extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        Intent i = getIntent();
        TextView tv = findViewById(R.id.Verifylog);
        Bundle args = i.getBundleExtra("BUNDLE");
        ArrayList<String> object = (ArrayList<String>) args.getSerializable("ARRAYLIST");
        tv.setText(object.toString());
    }
}
