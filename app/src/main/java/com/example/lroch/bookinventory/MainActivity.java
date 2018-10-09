package com.example.lroch.bookinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lroch.bookinventory.data.InventoryContract.InventoryEntry;


import com.example.lroch.bookinventory.data.InventoryDbHelper;


public class MainActivity extends AppCompatActivity {

    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new InventoryDbHelper(this);
        displayQueryData();

        //Used to insert book into database
        Button bookButton = findViewById(R.id.insert_book);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertBooks();
            }

        });

    }

    // List will refresh with new book info on data base
    @Override
    protected void onStart() {
        super.onStart();
        displayQueryData();
    }

    //Book info that will be inserted into Database
    private void insertBooks() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Harry Potter");
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 13);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 30);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, "Scholastic");
        values.put(InventoryEntry.COLUMN_PRODUCT_PHONE, "1-866-233-1692");

        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);
        Log.v("MainActivity", "New row ID" + newRowId);
    }

    private void displayQueryData() {

        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_PHONE
        };
        Cursor cursor = db.query(
                InventoryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        TextView displayView = (TextView) findViewById(R.id.inventory_text);
        try {
            displayView.setText("The books table contains " + cursor.getCount() + "books.\n\n ");
            displayView.append(InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_PRODUCT_NAME + " - " +
                    InventoryEntry.COLUMN_PRODUCT_PRICE + " - " +
                    InventoryEntry.COLUMN_PRODUCT_QUANTITY + " - " +
                    InventoryEntry.COLUMN_PRODUCT_SUPPLIER + " - " +
                    InventoryEntry.COLUMN_PRODUCT_PHONE + "\n");

            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PHONE);

            while (cursor.moveToNext()) {
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);

                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplier + " - " +
                        currentPhone
                ));
            }
        } finally {
            cursor.close();
        }
    }
}