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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Pet loader ID.
     */
    private static final int PET_LOADER = 0;

    /**
     * Pet adapter -- that binds UI to underlying pet database.
     */
    PetCursorAdapter petCursorAdapter;

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

        // Get list view to populate with pet data
        ListView petListView = findViewById(R.id.pet_list_view);

        // Bind empty view to pet list view -- so that it shows only when the list has 0 pets.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup pet adapter to create a list item for each row of pet data in the cursor
        // There is no pet data yet (until the loader finishes), so pass in null for the cursor
        petCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(petCursorAdapter);

        // Set up pet list item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> petListView, View clickedItem, int positionItem, long rowIdItem) {
                // Create intent for opening editor when pet is clicked
                Intent openPetEditorIntent = new Intent(CatalogActivity.this, EditorActivity.class);

                // For URI representing specific pet clicked
                Uri clickedPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, rowIdItem);

                // Set the URI on the data field of the intent
                openPetEditorIntent.setData(clickedPetUri);

                // Open editor for clicked pet
                startActivity(openPetEditorIntent);
            }
        });

        // Initialize the pet loader
        getSupportLoaderManager().initLoader(PET_LOADER, null, this);
    }

    // Implement cursor loader interface

    /**
     * Retrieve pet data from database as a cursor, when loader is created.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        // Create loader that will execute the content provider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,           //Provider content URI to query
                projection,                     // Columns to include in the resulting cursor
                null,                   // No selection clause
                null,               // No selection arguments
                null);                  // Default sort order
    }

    /**
     * Update UI with freshly retrieved pet data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> petLoader, Cursor petCursor) {
        // Update pet cursor with new cursor containing updated pet data
        petCursorAdapter.swapCursor(petCursor);
    }

    /**
     * Delete current pet data.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> petLoader) {
        // Detach current pet cursor -- that is, delete the current data
        petCursorAdapter.swapCursor(null);
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

        // Create row
        ContentValues petContentValues = new ContentValues();
        petContentValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        petContentValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        petContentValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        petContentValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert row
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, petContentValues);
    }
}
