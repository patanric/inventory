package com.patane.riccardo.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.patane.riccardo.inventory.data.ProductContract.ProductEntry;

/**
 * Created by riccardo on 27.02.17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public ProductCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.list_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_price);
        Button saleButton = (Button) view.findViewById(R.id.list_sale_button);
        saleButton.setTag(cursor.getPosition());

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME));
        int oldQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        String quant = context.getResources().getString(R.string.pieces, oldQuantity);
        float oldPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
        String price = context.getResources().getString(R.string.price, oldPrice);

        nameTextView.setText(name);
        quantityTextView.setText(quant);
        priceTextView.setText(price);
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Button saleButton = (Button) convertView.findViewById(R.id.list_sale_button);
//        saleButton.setTag(position);
//        return saleButton;
//    }
}
