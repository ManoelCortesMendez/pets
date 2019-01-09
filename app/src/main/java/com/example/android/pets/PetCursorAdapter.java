package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Adapter between the UI pet's list view and the underlying database that supplies it.
 *
 * The UI requests pets to fill the screen. The adapter retrieves the data from the database and
 * uses it to build the corresponding pets list items, which are then passed back to the UI.
 */
public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructor
     * @param context Environment data.
     * @param petCursor Pet data retrieved from the database as a cursor.
     */
    public PetCursorAdapter(Context context, Cursor petCursor) {
        super(context, petCursor,0);
    }

    /**
     * Inflate a blank new pet item view.
     *
     * @param context Environment data.
     * @param petCursor Pointing to current pet data.
     * @param parent Parent to which the new view is attached to.
     * @return the newly created pet item view.
     */
    @Override
    public View newView(Context context, Cursor petCursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View petItemView, Context context, Cursor petCursor) {

        // Get views that we want to edit within the pet list item view
        TextView petNameTextView = petItemView.findViewById(R.id.pet_name_text_view);
        TextView petBreedTextView = petItemView.findViewById(R.id.pet_breed_text_view);

        // Get indices of columns we're interested in
        int nameColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = petCursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);

        // Read pet data from cursor for current pet
        String petName = petCursor.getString(nameColumnIndex);
        String petBreed = petCursor.getString(breedColumnIndex);

        // Update the text views with the pet data
        petNameTextView.setText(petName);
        petBreedTextView.setText(petBreed);
    }

}
