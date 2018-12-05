package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Outer-class representing our pet app database.
 *
 * Declared final because it's just a class used to define constants, and we won't need to extend
 * or implement anything for this outer class.
 */
public final class PetContract {

    /**
     * Inner-class representing the pets table in our app's pets database.
     *
     * Our database only has one table.
     */
    public final static class PetEntry implements BaseColumns {

        /**
         * Table name
         */
        public final static String TABLE_NAME = "pets";

        /**
         * Column names
         */
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME = "name";
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "gender";
        public final static String COLUMN_PET_WEIGHT = "weights";

        /**
         * Helper constants for this table
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}
