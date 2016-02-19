package com.example.jake.maps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void startSightingMapActivity(View v){
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void startSightingsNearMeActivity(View v){
        Intent i = new Intent(this, SightingsNearMeActivity.class);
        startActivity(i);
    }
}
