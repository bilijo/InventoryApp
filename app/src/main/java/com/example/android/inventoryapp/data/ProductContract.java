package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

/**
 * Created by dam on 20.07.2017.
 */

public class ProductContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {}

    /**
     * Inner class that defines constant values for the products database table.
     */
    public static final class ProductEntry implements BaseColumns {

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QTY= "breed";

        /**
         * Price of the product.
         *
         * Type: DOUBLE
         */
        public final static String COLUMN_PRODUCT_PRICE = "Price";


    }


}
