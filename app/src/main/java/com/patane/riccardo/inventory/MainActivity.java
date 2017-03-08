package com.patane.riccardo.inventory;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;
import com.patane.riccardo.inventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int LOADER_ID = 5;
    private ProductCursorAdapter productCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputProductActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);

        View empty_view = findViewById(R.id.empty_view);
        listView.setEmptyView(empty_view);

        productCursorAdapter = new ProductCursorAdapter(this, null, 0);
        listView.setAdapter(productCursorAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }




//    @Override
//    protected void onStart() {
//        super.onStart();
//        displayDatabaseInfo();
//    }
//
//    private ProductDbHelper mDbHelper;
//
//    /**
//     * Temporary helper method to display information in the onscreen TextView about the state of
//     * the pets database.
//     */
//    private void displayDatabaseInfo() {
//        // Create and/or open a database to read from it
//        mDbHelper = new ProductDbHelper(this);
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                ProductEntry._ID,
//                ProductEntry.COLUMN_NAME,
//                ProductEntry.COLUMN_PRICE,
//                ProductEntry.COLUMN_QUANTITY,
//                ProductEntry.COLUMN_SUPPLIER };
//
//        // Perform a query on the pets table
//        Cursor cursor = db.query(
//                ProductEntry.TABLE_NAME,   // The table to query
//                projection,            // The columns to return
//                null,                  // The columns for the WHERE clause
//                null,                  // The values for the WHERE clause
//                null,                  // Don't group the rows
//                null,                  // Don't filter by row groups
//                null);                   // The sort order
//
//        TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//
//        try {
//            // Create a header in the Text View that looks like this:
//            //
//            // The pets table contains <number of rows in Cursor> pets.
//            // _id - name - breed - gender - weight
//            //
//            // In the while loop below, iterate through the rows of the cursor and display
//            // the information from each column in this order.
//            displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
//            displayView.append(ProductEntry._ID + " - " +
//                    ProductEntry.COLUMN_NAME + " - " +
//                    ProductEntry.COLUMN_PRICE + " - " +
//                    ProductEntry.COLUMN_QUANTITY + " - " +
//                    ProductEntry.COLUMN_SUPPLIER + "\n");
//
//            // Figure out the index of each column
//            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
//            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
//            int breedColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
//            int genderColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
//            int weightColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER);
//
//            // Iterate through all the returned rows in the cursor
//            while (cursor.moveToNext()) {
//                // Use that index to extract the String or Int value of the word
//                // at the current row the cursor is on.
//                int currentID = cursor.getInt(idColumnIndex);
//                String currentName = cursor.getString(nameColumnIndex);
//                String currentBreed = cursor.getString(breedColumnIndex);
//                int currentGender = cursor.getInt(genderColumnIndex);
//                int currentWeight = cursor.getInt(weightColumnIndex);
//                // Display the values from each column of the current row in the cursor in the TextView
//                displayView.append(("\n" + currentID + " - " +
//                        currentName + " - " +
//                        currentBreed + " - " +
//                        currentGender + " - " +
//                        currentWeight));
//            }
//        } finally {
//            // Always close the cursor when you're done reading from it. This releases all its
//            // resources and makes it invalid.
//            cursor.close();
//        }
//    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry._ID
        };
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        productCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productCursorAdapter.swapCursor(null);
    }
}
