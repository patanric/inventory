package com.patane.riccardo.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class).setData(uri);
                startActivity(intent);
            }
        });
    }

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


    public void trackSale(final View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("T: How many pieces?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                safeSale(v, input.getText().toString(), Integer.parseInt(v.getTag().toString()));
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

    private void safeSale(View view, String input, int which) {
        TextView quantTextView = (TextView) ((View) view.getParent()).findViewById(R.id.list_quantity);
        // TODO: take the quantity of the current listview item and not the first!!!
        Log.v(LOG_TAG, "TEST quantTextView: " + quantTextView);
        String piecesRaw = quantTextView.getText().toString();
        Log.v(LOG_TAG, "TEST pieces: " + piecesRaw);
        int pieces = Integer.parseInt(piecesRaw.substring(0, piecesRaw.length()-5));

        if (TextUtils.isEmpty(input)) {
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_QUANTITY, pieces-Integer.parseInt(input));

            Uri currentUri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, String.valueOf(which+1));
            Log.v(LOG_TAG, "TEST which: " + which);
            Log.v(LOG_TAG, "TEST currentUri: " + currentUri);
            int rowsAffected = 0;

            try {
                rowsAffected = getContentResolver().update(currentUri, contentValues, null, null);
                Log.v(LOG_TAG, "TEST rowsAffected: " + rowsAffected);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "TEST message: " + e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Display Toast after change was made.
            if (rowsAffected > 0) {
                Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_product_saving, Toast.LENGTH_LONG).show();
            }

        }
    }

}
