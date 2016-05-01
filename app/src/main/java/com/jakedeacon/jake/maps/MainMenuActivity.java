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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Main activity loaded when application starts.
 * Loads a list of scientific bird names from eBird List before allowing for search.
 * Allows for individual species search or all species by leaving autocomplete box empty.
 *
 * @author Jake Deacon
 * @version 1.1
 * @since 2016-04-31
 */

public class MainMenuActivity extends AppCompatActivity
        implements AsyncSciNameResponse {

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
    ArrayAdapter<String> autoAdapter;
    ArrayList<String> sciNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        this.curCon = this;
        this.imgButton = (ImageButton) findViewById(R.id.main_menu_sightings_near_me);
        this.progress = (ProgressBar) findViewById(R.id.main_menu_progress);
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
        this.autoText.setThreshold(1);
        this.autoText.setEnabled(false);
        this.imgButton.setEnabled(false);
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
        // (Handled in the SciNameProcessFinish)

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
        imgArr.recycle();
        return img;
    }

    /**
     * Handles search term verification that ensures that either NO value or an Autocomplete value
     * is selected to ensure no errors are thrown (BirdNotFound error returns from server if the
     * bird that is being searched for is not found. Scientific names are needed to search.)
     * @param v - The button clicked in the activity_main_menu.xml that calls this method.
     */
    public void startSightingsNearMeActivity(View v) {
        this.imgButton.setImageResource(R.drawable.button_pressed);

        if (autoVal == null || sciNames.contains(autoVal)) {
            autoVal = "";
            Intent i = new Intent(this, SightingsNearMeActivity.class);
            i.putExtra("species", autoVal);
            startActivity(i);
        } else if (!sciNames.contains(autoVal)) {
            // name is not contained in the list, don't start activity
            // pop Toast saying name not found when search button is hit
            Toast
                .makeText(this, R.string.error_name_not_found, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Overridden Asynchronous method that is implemented to handle the list of scientific names
     * that are returned to populate the Autocomplete Text box used for searching.
     * @param output - an ArrayList of Strings containing all the scientific names returned from
     *               eBird server.
     */
    @Override
    public void sciNameProcessFinish(ArrayList<String> output) {
        // copy list for verifications
        this.sciNames = output;
        // remove progress bar once loaded
        this.progress.setVisibility(View.GONE);
        // set up adapter
        autoAdapter =
                new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, output);
        // adapter attached to autotext
        this.autoText.setAdapter(autoAdapter);
        this.autoText.setEnabled(true);
        this.imgButton.setEnabled(true);
    }

    /**
     * Helper method that hides the built-in keyboard on screen. Called on item selection from
     * Autocomplete Text box
     * @param activity - The current activity that is displaying the keyboard to be hidden
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
