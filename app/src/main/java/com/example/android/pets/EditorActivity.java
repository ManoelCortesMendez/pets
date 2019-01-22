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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

// Import contract: directly import the inner class PetEntry to avoid typing PetContract.PetEntry each time
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    /** Identifier for the pet data loader */
    private static final int EXISTING_PET_LOADER = 0;

    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri currentPetUri;

    /** Variable that keeps track of whether the current pet has been edited */
    private boolean petHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the petHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View viewTouched, MotionEvent motionEvent) {
            petHasChanged = true;

            // We now return false to signify that the listener has NOT consumed the event,
            // because the click event is required to let us enter the edit field to change its value.
            // If consumed here, we wouldn't be able to edit any field!
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Check URI data passed with the intent to determine if:
        //  - We're creating a new pet
        //  - We're updating an existing pet
        Intent intent = getIntent();
        currentPetUri = intent.getData();

        // If the intent does not contain a pet content URI...
        if (currentPetUri == null) {
            // ... we need to create a new pet. So change the activity title to reflect that.
            setTitle("Add Pet");

            // ... and we need to remove the 'delete' item from the options menu, since we're
            // adding a new pet, and therefore deleting a pet that we have yet to add makes no sense.
            // Since 'delete' is the only item in our menu, we can remove the entire menu.
            // The following command makes the menu 'editable' -- the hiding itself is dealt with in
            // onPrepareOptionsMenu
            invalidateOptionsMenu();
        } else {
            // Else, we need to edit an existing pet. So change activity title to reflect that.
            setTitle("Edit Pet");

            // Initialize a loader to read the pet data from the database and display the current
            // values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user has touched
        // or modified them. This will let us know if there are unsaved changes or not, if the user
        // tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the 'Delete' menu item.
        if (currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true; // To display the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to the database
                savePet();
                // Exit activity and go up one level -- that is, go back to the pet list
                finish();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, navigate back to parent activity (CatalogActivity)
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise, if there are unsaved changes, set up a dialog to warn the user.
                // Create a click listener to handle the user confirming that the changes should
                // be discarded
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int buttonClicked) {
                        // User clicked 'Discard' button: navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message and click listeners for the positive
        // and negative buttons on the dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Discard your changes and quit editing?");
        alertDialogBuilder.setPositiveButton("Discard", discardButtonClickListener);
        alertDialogBuilder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int buttonClickedId) {
                // User clicked "Keep Editing", so dismiss the dialog and continue editing the pet
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this pet?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform deletion if this is an existing pet, not a new pet.
        if (currentPetUri != null) {
            // Call the content resolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the current pet URI already
            // identifies the pet we want.
            int nbRowsDeleted = getContentResolver().delete(currentPetUri, null, null);
            
            // Show toast message depending on whether or not the delete was successful.
            if (nbRowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error with deleting pet", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful.
                Toast.makeText(this, "Pet deleted", Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * Override activity's normal back button to deal with ongoing pet edits --- either discard
     * or continue editing the pet.
     */
    @Override
    public void onBackPressed() {

        // If the pet hasn't changed, continue with handling back button press
        if (!petHasChanged) {
            super.onBackPressed(); // Deal with the button press the normal way
            return; // early
        }

        // Otherwise, if there are unsaved changes, set up dialog to warn the user.
        // Create a click listener to handle the user confirming that the changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int buttonClickedId) {
                // User clicked discard button, so close current activity
                finish();
            }
        };

        // Show dialog saying that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Save new pet into the database or update an existing one.
     */
    private void savePet() {

        // Get values inputted by user
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // If all fields are empty, don't save an empty pet -- return early
        if (currentPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) { return; }

        // Create row with custom values
        ContentValues petContentValues = new ContentValues();
        petContentValues.put(PetEntry.COLUMN_PET_NAME, nameString);
        petContentValues.put(PetEntry.COLUMN_PET_BREED, breedString);
        petContentValues.put(PetEntry.COLUMN_PET_GENDER, mGender);

        // If weight field is empty, set weight to 0 instead
        int weight = 0;

        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        petContentValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // Determine if this is a new pet we're inserting, or an existing pet we're updating
        // by checking if currentPetUri is null (inserting) or not (updating)
        if (currentPetUri == null) {

            // This is a new pet, so insert a new pet into the provider,
            // returning the content URI for the new pet
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, petContentValues);

            // Handle insertion error/success
            if (newUri == null) {
                Toast.makeText(this, "Error saving pet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Pet saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise, we're updating and EXISTING pet. So update the pet with content URI: currentPetUri
            // and pass in the new content values. Pass in null for the selection and selection args
            // because currentPetUri will already identify the correct row in the database that we want to modify
            int rowsAffected = getContentResolver().update(currentPetUri, petContentValues, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update
                Toast.makeText(this, "Error updating pet", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful
                Toast.makeText(this, "Pet updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        // Since the editor shows all pet attributes, define a projection that contains all columns
        // from the pet table
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // This loader will execute the content provider's query method on a background thread
        return new CursorLoader(
                this,        // Parent activity context
                currentPetUri,       // Query the content URI for the current pet
                projection,         // Columns to include in the resulting cursor
                null,       // No selection clause
                null,   // No selection arguments
                null);     // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> petLoader, Cursor petCursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (petCursor == null || petCursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // This should be the only row in the cursor
        if (petCursor.moveToFirst()) {

            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Extract out the value from the cursor for the given column index
            String name = petCursor.getString(nameColumnIndex);
            String breed = petCursor.getString(breedColumnIndex);
            int gender = petCursor.getInt(genderColumnIndex);
            int weight = petCursor.getInt(weightColumnIndex);

            // Update the view on the screen with the values from the database
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // Gender is a dropdown spinner, so map the constant value from the database into one of
            // the dropdown options (0 is unknown, 1 is male, 2 is female).
            // Then call seSelection() so that option is displayed on screen aas the current selection.
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> petLoader) {
        // If loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}