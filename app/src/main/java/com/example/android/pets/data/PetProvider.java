package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    /**
     * Tag for log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Database helper object
     */
    private PetDbHelper petDbHelper;

    /**
     * Uri matcher object
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Uri matcher codes
     */
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    // Add patterns to matcher
    static {
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     *
     * @return true to signal that the provider loaded successfully.
     */
    @Override
    public boolean onCreate() {
        petDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Open readable pet database
        SQLiteDatabase petDatabase = petDbHelper.getReadableDatabase();

        // Match URI to determine what to do
        Cursor cursor;
        int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                // Perform full database query on pets table
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = petDatabase.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // Perform row query on pets table
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = petDatabase.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
        return cursor;
    }

    /**
     * Insert new data into the provider with the given content values.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        // Match URI to determine case
        final int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method for inserting a pet in the database.
     */
    private Uri insertPet(Uri uri, ContentValues contentValues) {

        // Data validation

        // Check that the name is not null
        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name!");
        }

        // Check that the gender isn't wrong
        Integer gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires a gender!");
        }

        // Check that the weight isn't wrong
        Integer weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalAccessError("Pet requires a valid weight!");
        }

        // No need to check the breed, since null is allowed

        // Open writable database
        SQLiteDatabase petDatabase = petDbHelper.getWritableDatabase();

        // Insert a new pet into the pets database table with the given ContentValues
        long id = petDatabase.insert(PetEntry.TABLE_NAME, null, contentValues);

        // Handle insertion failure -- denoted by an id of -1
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for" + uri);
            return null;
        }

        // Return the original URI with the id of the new pet appended
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Update the data at the given selection and selection arguments, with the new content values.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Return the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
