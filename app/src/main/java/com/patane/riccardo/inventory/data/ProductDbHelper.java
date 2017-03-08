package com.patane.riccardo.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

/**
 * Created by riccardo on 08.03.17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "products.db";

    public static final String TEXT = " TEXT";
    public static final String NOT_NULL = " NOT NULL";
    public static final String INTEGER = " INTEGER";
    public static final String COMMA_SEPARATOR = ",";

    public static final String CREATE_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME +
            "(" + ProductEntry._ID + INTEGER + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEPARATOR +
            ProductEntry.COLUMN_NAME + TEXT + NOT_NULL + COMMA_SEPARATOR +
            ProductEntry.COLUMN_PRICE + INTEGER + COMMA_SEPARATOR +
            ProductEntry.COLUMN_QUANTITY + INTEGER + COMMA_SEPARATOR +
            ProductEntry.COLUMN_SUPPLIER + INTEGER + COMMA_SEPARATOR +
            ProductEntry.COLUMN_IMAGE + INTEGER + ")";

    public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;


    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }
}
