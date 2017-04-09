package com.patane.riccardo.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InputProductActivity extends AppCompatActivity {

    private static final String LOG_TAG = InputProductActivity.class.getSimpleName();
    private ImageView mImageView;
    private static final int CAMERA_REQUEST = 1888;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private Button pictureButton;
    private String mCurrentPhotoPath;

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

    // End of class variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_product);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mImageView = (ImageView) findViewById(R.id.picture);
        pictureButton = (Button) findViewById(R.id.take_picture);
        pictureButton.setOnTouchListener(mTouchListener);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "Error occurred while creating the File", ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoUri = FileProvider.getUriForFile(InputProductActivity.this, "com.patane.riccardo.fileprovider", photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }

                }
            }
        });

        mNameEditText = (EditText) findViewById(R.id.input_product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText = (EditText) findViewById(R.id.input_quantity);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText = (EditText) findViewById(R.id.input_price);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText = (EditText) findViewById(R.id.input_supplier);
        mSupplierEditText.setOnTouchListener(mTouchListener);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void loadImageFromFile() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            loadImageFromFile();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
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
                        NavUtils.navigateUpFromSameTask(InputProductActivity.this);
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

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_NAME, nameString);
        contentValues.put(ProductEntry.COLUMN_QUANTITY, quantityString);
        contentValues.put(ProductEntry.COLUMN_PRICE, priceString);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER, supplierString);
        contentValues.put(ProductEntry.COLUMN_IMAGE, mCurrentPhotoPath);


        Uri newUri = null;
        try {
            newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (newUri != null) {
            Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_product_saving, Toast.LENGTH_LONG).show();
        }
    }
}
