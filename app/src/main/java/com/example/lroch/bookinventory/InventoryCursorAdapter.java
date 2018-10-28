package com.example.lroch.bookinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.lroch.bookinventory.data.InventoryContract;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView titleTextView = (TextView) view.findViewById(R.id.book_title);
        final TextView bookPriceTextView = view.findViewById(R.id.book_price);
        final TextView bookQuantityTextView = view.findViewById(R.id.book_quantity);
        final Button purchaseBookBtn = view.findViewById(R.id.purchase_book_btn);

        //Find the columns of interest from the Cursor
        int title = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int price = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        final int quantity = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        final String id = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        //Read inventory attributes from the cursor for the current book
        String bookTitle = cursor.getString(title);
        String bookPrice = cursor.getString(price);
        final int bookQuantity = cursor.getInt(quantity);

        //Update the TextViews for the current book
        titleTextView.setText(bookTitle);
        bookPriceTextView.setText(bookPrice);
        bookQuantityTextView.setText(String.valueOf(bookQuantity));

        if(bookQuantity <= 0) {
            bookQuantityTextView.setText("0");
            outOfStock(view);
        }
        purchaseBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentBookUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, Long.parseLong(id));
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, bookQuantity - 1);
                context.getContentResolver().update(currentBookUri, values, null, null);

            }
        });
    }
    /**
     *Source for Snackbar:
     * https://developer.android.com/training/snackbar/showing
     * https://stackoverflow.com/search?q=findviewbyid+error+snackbar
     * https://stackoverflow.com/questions/45116211/snack-bar-with-navigation-drawer-activity-not-working
     * */
    public void outOfStock(View view){
        View coordinatorLayout = view.findViewById(R.id.myCoordinatorLayout);
        Snackbar.make(coordinatorLayout,R.string.out_of_stock,Snackbar.LENGTH_SHORT).show();
    }
}