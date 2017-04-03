package com.patane.riccardo.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private ImageView mImageView;
    private Uri mCurrentProductUri;
    private String imagePath;

    private final int LOADER_ID = 6;

    // variable to notify if field have changed.
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    private DialogInterface.OnClickListener keepButtonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the pet.
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    };
    // END of class variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mCurrentProductUri = getIntent().getData();

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText = (EditText) findViewById(R.id.detail_supplier);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mImageView = (ImageView) findViewById(R.id.detail_image);
        mImageView.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
//                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener buttonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(buttonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener buttonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };

        showUnsavedChangesDialog(buttonClickListener);
    }

    // create a "discard changes" dialog:
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Discard your changes and quit editing?");
        dialogBuilder.setPositiveButton("Discard", discardButtonClickListener);
        dialogBuilder.setNegativeButton("Keep editing", keepButtonClickListener);
        // Create and show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString)) {
            finish();

        } else {
//            String productName = nameString;
//            String quantity = quantityString;
//            int weight = 0;
//            if (!TextUtils.isEmpty(weightString)) {
//                weight = Integer.parseInt(weightString);
//            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_NAME, nameString);
            contentValues.put(ProductEntry.COLUMN_QUANTITY, quantityString);
            contentValues.put(ProductEntry.COLUMN_PRICE, priceString);
            contentValues.put(ProductEntry.COLUMN_SUPPLIER, supplierString);
            contentValues.put(ProductEntry.COLUMN_IMAGE, imagePath);

            Uri newUri = null;
            int rowsAffected = 0;

            mCurrentProductUri = getIntent().getData();
            if (mCurrentProductUri == null) {
//                newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
                Log.e(LOG_TAG, "This part of the IF statement was not supposed to be used for this activity.");
            } else {
                try {
                    rowsAffected = getContentResolver().update(mCurrentProductUri, contentValues, null, null);

                    // Display Toast after change was made.
                    if (newUri != null || rowsAffected > 0) {
                        Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.error_product_saving, Toast.LENGTH_LONG).show();
                    }

                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void deleteItem() {
        if (mCurrentProductUri != null) {
            int deletedRows = getContentResolver().delete(mCurrentProductUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(DetailActivity.this, R.string.delete_product_successfully, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.delete_products_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    // create a delete confirmation dialog:
    private void showDeleteConfirmationDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.dele_this_product);
        alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // don't delete pet and close dialog.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog deleteAlertDialog = alertDialogBuilder.create();
        deleteAlertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_SUPPLIER,
                ProductEntry.COLUMN_IMAGE,
                ProductEntry._ID
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_NAME);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int supplierColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPPLIER);
            int imageColumnIndx = data.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int quantiy = data.getInt(quantityColumnIndex);
            float price = data.getFloat(priceColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            imagePath = data.getString(imageColumnIndx);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantiy + "");
            mPriceEditText.setText(String.valueOf(price));
            mSupplierEditText.setText(supplier);
            loadImageFromFile();
        }
    }

    private void loadImageFromFile() {
        // Get the dimensions of the View
        Log.v(LOG_TAG, "TEST mImageView: " + mImageView.toString());
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        Log.v(LOG_TAG, "TEST targetW: " + targetW);
        Log.v(LOG_TAG, "TEST targetH: " + targetH);
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mQuantityEditText.setText(null);
        mPriceEditText.setText(null);
        mSupplierEditText.setText(null);
    }
}
