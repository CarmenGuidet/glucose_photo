package com.carmenguidetgomez.glucemyphoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;

public class NewMenuActivity extends AppCompatActivity {
    Button btn_new_sample;
    Button btn_oldsamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_menu);

        btn_new_sample = findViewById(R.id.buttonNewSample);
        btn_oldsamples = findViewById(R.id.buttonOldSamples);

        btn_new_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMenuActivity.this, ProcessedActivity.class);
                startActivity(intent);
            }
        });

        btn_oldsamples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


}
