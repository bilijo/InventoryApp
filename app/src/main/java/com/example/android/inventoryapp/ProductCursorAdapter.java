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
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.inventoryapp.data.ProductDbHelper;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.content.ContentUris.withAppendedId;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.COLUMN_PRODUCT_QTY;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.TABLE_NAME;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry._ID;
import static java.security.AccessController.getContext;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {
    /**
     * Database helper object
     */
    private ProductDbHelper mDbHelper;



    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_product.xml
        return LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list product layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.text_product_name);
        final TextView qtyTextView = (TextView) view.findViewById(R.id.text_product_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_product_price);
        //  TextView emailTextView = (TextView) view.findViewById(R.id.text_product_email);
        // TextView imageTextView = (TextView) view.findViewById(R.id.edit_product_image);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        final int qtyColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_QTY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        //  int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        //int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        final String productQty = cursor.getString(qtyColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        // String productEmail= cursor.getString(emailColumnIndex);
        // String productImage = cursor.getString(imageColumnIndex);

        // If the product price is empty string or null, then use some default text
        // that says "Unknown price", so the TextView isn't blank.
        if (TextUtils.isEmpty(productPrice)) {
            productPrice = context.getString(R.string.unknown_price);
        }


        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        qtyTextView.setText(productQty);
        // emailTextView.setText(productEmail);
        //imageTextView.setText(productImage);


        // Setup the sale product button click listener
        Button decreaseQty = (Button) view.findViewById(R.id.sale_button);
        final int position = cursor.getPosition();
        decreaseQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);

                int minusQty = cursor.getInt(qtyColumnIndex);
                if (minusQty > 0) {
                    minusQty--;
                    String minusText = String.valueOf(minusQty);
                    // update textview
                    qtyTextView.setText(minusText);

                    int indx = cursor.getColumnIndex(ProductEntry._ID);

                    //ProductDbHelper mDbHelper = new ProductDbHelper(context);

                    int idRow = cursor.getInt(indx);
                    Uri currentProductUri = withAppendedId(ProductEntry.CONTENT_URI, idRow);

                    Log.d("LOG_TAG", "rowId: " + idRow);

                    // Get writeable database to store the new quantity
                    //SQLiteDatabase database = mDbHelper.getWritableDatabase();


                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PRODUCT_QTY, minusQty);
                    Log.d("LOG_TAG", "values: " + values+" indx="+indx);
                  // Uri ContentUris = withAppendedId(ProductEntry.CONTENT_URI, 2);

                //  database.update(TABLE_NAME, values, "_id="+idRow, null);
                context.getContentResolver().update(currentProductUri, values, null, null);


                } else {
                    Toast.makeText(context, R.string.sale_quantity_no_match, Toast.LENGTH_SHORT).show();
                }
            }
        });
       // cursor.close();

    }

}