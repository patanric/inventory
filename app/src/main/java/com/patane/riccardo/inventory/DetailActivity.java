package com.patane.riccardo.inventory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class DetailActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mSupplierEditText = (EditText) findViewById(R.id.detail_supplier);

    }
}
