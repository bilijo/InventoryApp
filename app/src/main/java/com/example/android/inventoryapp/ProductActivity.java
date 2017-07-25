package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by dam on 20.07.2017.
 */

public class ProductActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ProductActivity.class.getSimpleName();
    // Image to display in layout view
    public static final int IMAGE_GALLERY_REQUEST = 20;
    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    Context context;
    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;
    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the product's Quantity
     */
    private EditText mQtyEditText;
    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the product supplier's email
     */
    private EditText mEmailEditText;
    /**
     * EditText field to enter the product image
     */
    private EditText mImageEditText;
    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    private ImageView imgPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Where to show the image
        imgPicture = (ImageView) findViewById(R.id.product_image);


        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        Log.d(LOG_TAG, "intent.getData(): " + mCurrentProductUri);

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.product_activity_title_new_product));

            // hide the "Delete" button.
            Button BtDelete = (Button) findViewById(R.id.btn_delete_product);
            BtDelete.setVisibility(View.INVISIBLE);

            // hide the "increase" qty button.
            Button BtPlus = (Button) findViewById(R.id.btn_qty_plus);
            BtPlus.setVisibility(View.INVISIBLE);

            // hide the "decrease" qty button.
            Button BtMinus = (Button) findViewById(R.id.btn_qty_minus);
            BtMinus.setVisibility(View.INVISIBLE);



        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.product_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQtyEditText = (EditText) findViewById(R.id.edit_product_qty);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mEmailEditText = (EditText) findViewById(R.id.edit_product_email);
        mImageEditText = (EditText) findViewById(R.id.edit_product_image);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQtyEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);

        // Setup the save button click listener
        Button saveButton = (Button) findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        // Setup the delete button click listener
        Button deleteButton = (Button) findViewById(R.id.btn_delete_product);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();

            }
        });

        // only for debugging
        final String dataEmail = "product@supplier.com";
        // Setup the email order button click listener
        Button emailButton = (Button) findViewById(R.id.btn_supplier);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(dataEmail);

            }
        });
    }


    /**
     * Image Picker
     * This method will be invoked when the user clicks the button to choose an image
     *
     * @param v
     */
    public void onImageGalleryClicked(View v) {
        // invoke the image gallery using an implict intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // Address of the image on the SD Card.
                Uri imageUri = data.getData();
                //String imagePath = imageUri.getPath();
                String imagePath = imageUri.toString();

                // declare a stream to read the image data from the SD Card.
                InputStream inputStream;

                // Getting an input stream, based on the URI of the image.
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // Get a bitmap from the stream.
                    Bitmap image = BitmapFactory.decodeStream(inputStream);

                    // show the image to the user
                    // imgPicture.setImageBitmap(image);
                    imgPicture.setImageURI(imageUri);

                    mImageEditText.setText(imagePath);
                    Log.d(LOG_TAG, "imageUri :" + imageUri);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // show a message to the user indictating that the image is unavailable.
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


    /**
     * Intent object to launch Email client to send an Email to the given recipients.
     */
    protected void sendEmail(String vEmail) {
        Log.i("Send email", "");
        String[] TO = {vEmail};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:" + vEmail));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i(LOG_TAG, "Finished sending email...");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ProductActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String qtyString = mQtyEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String imageString = mImageEditText.getText().toString().trim();
         imgPicture.setImageURI(Uri.parse(imageString));

        // Check if name fields in the editor are blank

        while (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.add_name_to_product),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        /*
        while (TextUtils.isEmpty(emailString)){
            Toast.makeText(this, getString(R.string.add_email_to_product),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        */
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageString);


        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int qty = 0;
        if (!TextUtils.isEmpty(qtyString)) {
            qty = Integer.parseInt(qtyString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QTY, qty);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);

        // If the email is not provided by the user, don't try to parse the string into an
        // string value. Use 0 by default.
        String email = "product@supplier.com";
        if (!TextUtils.isEmpty(emailString)) {
            email = emailString;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, email);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            Log.d(LOG_TAG, "saveProduct newUri: " + newUri);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }


        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            Log.d(LOG_TAG, "mCurrentProductUri: " + mCurrentProductUri);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    /**
     * increase quantity.
     */
    public int plusQuantity(int intQty) {
        // check if Qty >= 0

        if (intQty >= 0) {

            intQty++; // increase by 1
        } else {
            Toast.makeText(this, getString(R.string.editor_quantity_no_match),
                    Toast.LENGTH_SHORT).show();
        }
        return intQty;
    }

    /**
     * decrease quantity.
     */
    public int minusQuantity(int intQty) {
        // check if Qty >= 0

        if (intQty > 0) {

            intQty--; // decrease by 1
        } else {
            Toast.makeText(this, getString(R.string.editor_quantity_no_match),
                    Toast.LENGTH_SHORT).show();
        }
        return intQty;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QTY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            final int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            final int qty = cursor.getInt(qtyColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String image = cursor.getString(imageColumnIndex);


            //Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQtyEditText.setText(String.valueOf(qty));
            mPriceEditText.setText(String.valueOf(price));
            mEmailEditText.setText(String.valueOf(email));
            mImageEditText.setText(String.valueOf(image));

            // Setup the minus quantity button click listener
            Button minusQtyButton = (Button) findViewById(R.id.btn_qty_minus);
            minusQtyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mQtyInteger = Integer.parseInt(String.valueOf(mQtyEditText.getText().toString()));

                    mQtyEditText.setText(String.valueOf(minusQuantity(mQtyInteger)));
                }
            });
            // Setup the plus quantity button click listener
            Button plusQtyButton = (Button) findViewById(R.id.btn_qty_plus);
            plusQtyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mQtyInteger = Integer.parseInt(String.valueOf(mQtyEditText.getText().toString()));

                    mQtyEditText.setText(String.valueOf(plusQuantity(mQtyInteger)));
                }
            });

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQtyEditText.setText("");
        mPriceEditText.setText("");
        mEmailEditText.setText("");
        mImageEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        // See mCurrentProductUri in onCreateLoader() method
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Toast.makeText(this, getString(R.string.editor_delete_product_successful),Toast.LENGTH_SHORT).show();
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }

}
