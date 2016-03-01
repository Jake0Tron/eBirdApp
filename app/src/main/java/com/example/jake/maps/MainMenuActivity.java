package com.example.jake.maps;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    ImageView mainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        this.mainImage = (ImageView) findViewById(R.id.main_menu_image);
        this.mainImage.setImageDrawable(getRandomDrawable());
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mainImage = (ImageView) findViewById(R.id.main_menu_image);
        this.mainImage.setImageDrawable(getRandomDrawable());
    }

    Drawable getRandomDrawable() {

        Drawable img;
        TypedArray imgArr = getResources().obtainTypedArray(R.array.img_id_array);
        int size = imgArr.length();
        Random r = new Random();
        int gottenID = r.nextInt(size);
        img = imgArr.getDrawable(gottenID);
        return img;
    }

    public void startSightingsNearMeActivity(View v) {
        Intent i = new Intent(this, SightingsNearMeActivity.class);
        startActivity(i);
    }
}
