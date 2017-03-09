package com.patane.riccardo.inventory;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private Uri mCurrentProductUri;

    private final int LOADER_ID = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mCurrentProductUri = getIntent().getData();

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mSupplierEditText = (EditText) findViewById(R.id.detail_supplier);

        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_SUPPLIER,
                ProductEntry._ID
        };
        Log.v(LOG_TAG, "URI: " + mCurrentProductUri);
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_NAME);
        int quantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int supplierColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPPLIER);

        data.moveToFirst();
        // Extract out the value from the Cursor for the given column index
        String name = data.getString(nameColumnIndex);
        int quantiy = data.getInt(quantityColumnIndex);
        float price = data.getFloat(priceColumnIndex);
        String supplier = data.getString(supplierColumnIndex);

        // Update the views on the screen with the values from the database
        mNameEditText.setText(name);
        mQuantityEditText.setText(quantiy + "");
        mPriceEditText.setText(String.valueOf(price));
        mSupplierEditText.setText(supplier);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mQuantityEditText.setText(null);
        mPriceEditText.setText(null);
        mSupplierEditText.setText(null);
    }
}
