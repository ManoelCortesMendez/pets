/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    /**
     * Database helper object to interact with database.
     */
    private PetDbHelper mDbHelper;

    /**
     * Method called when activity is created to set its content and create the app database.
     *
     * @param savedInstanceState Saved previous state, if there is any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To create our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);

        // Show information about the database
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        // Define projection -- that is a string array with the names of the columns we're interested in
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // Query for full database using the content resolver
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,   // The content URI of the pets table
                projection,             // the columns to return for each row
                null,          // Selection criteria
                null,       // Selection criteria
                null);         // The sort order for the returned rows

        try {
            // Get main text view
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);

            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            displayView.setText("Number of rows in pets database table: " + cursor.getCount() + "\n\n");

            // Display cursor header row
            displayView.append(PetEntry._ID + " --- " +
                    PetEntry.COLUMN_PET_NAME + " --- " +
                    PetEntry.COLUMN_PET_BREED + " --- " +
                    PetEntry.COLUMN_PET_GENDER + " --- " +
                    PetEntry.COLUMN_PET_WEIGHT + "\n");

            // Get column indices
            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Iterate through cursor lines
            while (cursor.moveToNext()) {
                // Get values of each field in the current cursor line
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender = cursor.getInt(genderColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                // Display values in activity -- that is, build the table line by line
                displayView.append("\n" +
                        currentId + " --- " +
                        currentName + " --- " +
                        currentBreed + " --- " +
                        currentGender + " --- " +
                        currentWeight);
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Inflate and display menu.
     *
     * @param menu Reference to the on-screen menu anchor.
     * @return true to display the menu inflated on screen.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true; // This adds menu items to the app bar.
    }

    /**
     * Map behaviour to each menu item.
     *
     * @param item Menu item that was clicked.
     * @return true to confirm that we handled the behaviour in this method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Inset dummy pet in database
                insertDummyPet();
                // Update database info on screen
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Insert a dummy pet, with pre-defined data, into the database.
     */
    private void insertDummyPet() {

        // Get database in write mode
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Create row
        ContentValues petContentValues = new ContentValues();
        petContentValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        petContentValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        petContentValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        petContentValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Add row to database
        long newRowId = database.insert(PetEntry.TABLE_NAME, null, petContentValues);

        // Debug: print log message confirming the creation of a new row in the database
        Log.e("CatalogActivity", "New row ID: " + newRowId);
    }
}
