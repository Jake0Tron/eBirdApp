package com.jakedeacon.jake.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Random;

public class MainMenuActivity extends AppCompatActivity
implements AsyncSciNameResponse{

    ImageView mainImage;
    ImageButton imgButton;
    // autocomplete for search
    AutoCompleteTextView autoText;
    String autoVal;
    // progress bar while loading
    ProgressBar progress;
    URLBuilder uBuild;
    AsyncSciNameTask sciNameTask;
    Activity curCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        this.curCon = this;
        this.imgButton = (ImageButton) findViewById(R.id.main_menu_sightings_near_me);

        this.progress = (ProgressBar) findViewById(R.id.main_menu_progress);
        // will be enabled in the activity when loading commences
        this.progress.setVisibility(View.GONE);
        this.progress.setMax(100);
        this.autoText = (AutoCompleteTextView) findViewById(R.id.main_menu_auto);
        // handle selection of auto complete suggestion
        this.autoText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoVal = (String) parent.getItemAtPosition(position);
                hideSoftKeyboard(curCon);
            }
        });

        // set up autotext to read from CSV and finish based on content from list
        // LOGIC: get URL from builder for list of all bird names (for now species and broad species will be used
        // send url to AsyncSciNameTask to return a list of birds to be used to populate autocomplete
        // takes activity context to allow access to UI
        this.sciNameTask = new AsyncSciNameTask(this);
        this.uBuild = new URLBuilder();
        this.sciNameTask.setDelegate(this);
        String url = uBuild.getListOfAlleBirds();
        this.sciNameTask.execute(url);
        // wait for list of birds to be compiled (loading bar or something similar)
        // attach list to autocomplete to allow for searching by sci names provided
        // on selection send the scientific name to next activity to determine what will be drawn on the map
        // (Handled in the SciNameProcessFInish)

        this.mainImage = (ImageView) findViewById(R.id.main_menu_image);
        this.mainImage.setImageDrawable(getRandomDrawable());
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mainImage = (ImageView) findViewById(R.id.main_menu_image);
        this.mainImage.setImageDrawable(getRandomDrawable());
        this.imgButton.setImageResource(R.drawable.button);
        this.autoText.setText(null);
        this.autoVal = "";
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
        this.imgButton.setImageResource(R.drawable.button_pressed);

        if (autoVal == null)
            autoVal = "";
        Log.d("SPECIES", autoVal);
        Intent i = new Intent(this, SightingsNearMeActivity.class);
        i.putExtra("species", autoVal);
        startActivity(i);
    }

    @Override
    public void sciNameProcessFinish(ArrayList<String> output) {
        // remove progress bar once loaded
        this.progress.setVisibility(View.GONE);
        // set up adapter
        ArrayAdapter<String> autoAdapter =
                new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item , output);
        // adapter attached to autotext
        this.autoText.setAdapter(autoAdapter);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
