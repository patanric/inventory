package com.patane.riccardo.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.y;

/**
 * Created by riccardo on 05.03.17.
 */

public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCTS_ID = 200;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
    }

    ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Cursor for the result of the queries
        Cursor cursor;

        // RUI matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.

                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // this is used to set a "watcher" for when something in the SQL table is changed.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product requires a name");
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        long id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int rowsDeleted;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCTS_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        Log.v(LOG_TAG, "TEST contentValues: " + contentValues.toString());

        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // Check that the name is not null
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Product requires valid name");
            }
        }

        // check that price is not null
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRICE)) {
            Float price = contentValues.getAsFloat(ProductContract.ProductEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // Check that the quantity is not null
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // Check that the supplier is not null
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER)) {
            String supplier = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER);
            if (supplier == null || supplier.isEmpty()) {
                throw new IllegalArgumentException("Product requires valid supplier");
            }
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int rowsUpdated = db.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
