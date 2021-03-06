package com.patane.riccardo.inventory;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private EditText mNameEditText;
    private TextView mQuantityTextView;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private ImageView mImageView;
    private Uri mCurrentProductUri;
    private String dbImagePath;
    private TextView mClickHere;
    private String mChange;
    private Button mPlusButton;
    private Button mMinusButton;

    private static final int CAMERA_REQUEST = 1555;
    private static final int PICK_IMAGE = 34;
    private final int LOADER_ID = 6;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4646;
    private final int TRANSPARENT = 0x00000000;

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

    private View.OnClickListener fotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProductHasChanged = true;
            pickImage();
        }
    };

    private DialogInterface.OnClickListener galleryButtonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // check if permission is granted.
            if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(DetailActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(DetailActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                pickGallery();
            }

        }
    };

    private DialogInterface.OnClickListener camButtonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            pickCamera();
        }
    };
    // END of class variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView = (TextView) findViewById(R.id.detail_quantity);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText = (EditText) findViewById(R.id.detail_supplier);
        mSupplierEditText.setOnTouchListener(mTouchListener);

        mPlusButton = (Button) findViewById(R.id.plus_button);
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton = (Button) findViewById(R.id.minus_button);
        mMinusButton.setOnTouchListener(mTouchListener);

        mImageView = (ImageView) findViewById(R.id.detail_image);
//        mImageView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mClickHere = (TextView) findViewById(R.id.detail_add_pic);
        mClickHere.setOnClickListener(fotoClickListener);

        mCurrentProductUri = getIntent().getData();
        if (mCurrentProductUri == null) {
            setTitle(getResources().getString(R.string.detail_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getResources().getString(R.string.detail_activity_title_edit_product));
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void pickCamera() {
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
                Uri photoUri = FileProvider.getUriForFile(DetailActivity.this, "com.patane.riccardo.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    pickGallery();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
        dbImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    // onPrepareOptionsMenu is called after a options menu invalidation (invalidateOptionsMenu()).
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (mProductHasChanged) {
                    saveItem();
                } else finish();
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

    // create a "pick image" dialog:
    private void pickImage() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Where do you want the image from?");
        dialogBuilder.setPositiveButton("Gallery", galleryButtonClickListener);
        dialogBuilder.setNegativeButton("Take picture", camButtonClickListener);
        // Create and show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString)) {
            finish();

        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_NAME, nameString);
            contentValues.put(ProductEntry.COLUMN_QUANTITY, quantityString);
            contentValues.put(ProductEntry.COLUMN_PRICE, priceString);
            contentValues.put(ProductEntry.COLUMN_SUPPLIER, supplierString);
            contentValues.put(ProductEntry.COLUMN_IMAGE, dbImagePath);

            Uri newUri = null;
            int rowsAffected = 0;

            mCurrentProductUri = getIntent().getData();
            if (mCurrentProductUri == null) {
                try {
                    newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                try {
                    rowsAffected = getContentResolver().update(mCurrentProductUri, contentValues, null, null);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            // Display Toast after change was made.
            if (newUri != null || rowsAffected > 0) {
                Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_product_saving, Toast.LENGTH_LONG).show();
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

    public void increaseQuant(View v) {
        int quant = Integer.parseInt(mQuantityTextView.getText().toString());
        quant++;
        mQuantityTextView.setText(String.valueOf(quant));
    }

    public void reduceQuant(View v) {
        int quant = Integer.parseInt(mQuantityTextView.getText().toString());
        quant--;
        mQuantityTextView.setText(String.valueOf(quant));
    }

    public void orderIntent(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How many pieces?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChange = input.getText().toString();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setType("*/*");
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Ordering " + mChange + " pcs. of " + mNameEditText.getText());
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            loadImageFromFile();
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                Log.v(LOG_TAG, "VERSION < 11");
                dbImagePath = RealPathUtils.getRealPathFromURI_BelowAPI11(this, data.getData());
            }
            // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19) {
                Log.v(LOG_TAG, "VERSION 11-18");
                dbImagePath = RealPathUtils.getRealPathFromURI_API11to18(this, data.getData());
            }
            // SDK > 19 (Android 4.4)
            else{
                Log.v(LOG_TAG, "VERSION 19+");
                dbImagePath = RealPathUtils.getRealPathFromURI_API19(this, data.getData());
            }

            loadImageFromFile();
        }
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
            String dbName = data.getString(nameColumnIndex);
            int dbQuantiy = data.getInt(quantityColumnIndex);
            float dbPrice = data.getFloat(priceColumnIndex);
            String dbSupplier = data.getString(supplierColumnIndex);
            dbImagePath = data.getString(imageColumnIndx);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(dbName);
            String texxt = getResources().getString(R.string.quantity, dbQuantiy);
            mQuantityTextView.setText(texxt);
            mPriceEditText.setText(String.valueOf(dbPrice));
            mSupplierEditText.setText(dbSupplier);
            loadImageFromFile();
            // TODO: image does not show when storage permission is off!
        }
    }

    private void loadImageFromFile() {
        if (dbImagePath != null) {

            // Get the dimensions of the View
            int targetW = mImageView.getLayoutParams().width;
            int targetH = mImageView.getLayoutParams().height;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(dbImagePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            Log.v(LOG_TAG, "TEST targetW: " + targetW);
            Log.v(LOG_TAG, "TEST targetH: " + targetH);
            Log.v(LOG_TAG, "TEST mImageView: " + mImageView);
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(dbImagePath, bmOptions);

            // rotate bitmap if the orientation is wrong.
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(dbImagePath);
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR message: " + e.getMessage());
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Bitmap rotatedBitmap = null;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                mImageView.setImageBitmap(rotatedBitmap);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                mImageView.setImageBitmap(rotatedBitmap);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                mImageView.setImageBitmap(rotatedBitmap);
            } else {
                mImageView.setImageBitmap(bitmap);
            }

            mImageView.setBackgroundColor(TRANSPARENT);
            mClickHere.setVisibility(View.INVISIBLE);
            mImageView.setOnClickListener(fotoClickListener);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mQuantityTextView.setText(null);
        mPriceEditText.setText(null);
        mSupplierEditText.setText(null);
    }
}
