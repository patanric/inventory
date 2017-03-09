package com.patane.riccardo.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

public class InputProductActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int CAMERA_REQUEST = 1888;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_product);

        imageView = (ImageView) findViewById(R.id.picture);
        Button pictureButton = (Button) findViewById(R.id.take_picture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        mNameEditText = (EditText) findViewById(R.id.input_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.input_quantity);
        mPriceEditText = (EditText) findViewById(R.id.input_price);
        mSupplierEditText = (EditText) findViewById(R.id.input_supplier);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap picture = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(picture);
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
            case R.id.home:
                // TODO
                return true;
        }

        return super.onOptionsItemSelected(item);
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

        Uri newUri = null;
        newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

        if (newUri != null) {
            Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_product_saving, Toast.LENGTH_LONG).show();
        }
    }
}
