package com.carmenguidetgomez.glucemyphoto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InfoActivity extends AppCompatActivity {
    private Button back;
    private Button botongithub;
    String _url = "https://github.com/CarmenGuidet/glucose_photo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        botongithub = findViewById(R.id.buttonGithub);
        botongithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri _link = Uri.parse(_url);
                Intent intent = new Intent(Intent.ACTION_VIEW,_link);
                startActivity(intent);
            }
        });
    }
}