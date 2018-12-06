package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.android.pets.data.PetContract.PetEntry; // To leverage our contract constants

/**
 * Helper class to create a new database, connect to and existing database, and manages updating
 * the database schema if the version changes.
 *
 * This class isn't used for CRUD operations though. todo: is this line true?
 */
public class PetDbHelper extends SQLiteOpenHelper {

    /**
     * Essential database variables.
     */
    public static final int DATABASE_VERSION = 1; // Starts at 1 by convention
    public static final String DATABASE_NAME = "shelter.db";

    /**
     * Database helper class constructor.
     *
     * @param context General info about the current state of the app.
     */
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when a database is created for the first time. Creates the database tables and
     * initial content.
     *
     * @param database The database to populate.
     */
    public void onCreate(SQLiteDatabase database) {

        String COMMA = ", ";

        // Build SQL statement for creating table
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                        PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                        PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL" + COMMA +
                        PetEntry.COLUMN_PET_BREED + " TEXT" + COMMA +
                        PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL" + COMMA +
                        PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0" + ");";

        // Check SQL statements syntax
        Log.e("SQL_CREATE_ENTRIES: ", SQL_CREATE_ENTRIES);

         // Create table and populate database with it
        database.execSQL(SQL_CREATE_ENTRIES);
    }
    
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // This database is only a cache for pet data, so its upgrade policy is simply to start over:
        // delete all tables in the database and create new tables from scratch

        // Build SQL statement for deleting table
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;

        // Check SQL statements syntax
        Log.e("SQL_DELETE_ENTRIES: ", SQL_DELETE_ENTRIES);

        database.execSQL(SQL_DELETE_ENTRIES); // Delete database tables
        onCreate(database); // Create database tables
    }
}
