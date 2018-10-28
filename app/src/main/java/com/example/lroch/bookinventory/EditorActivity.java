package com.example.lroch.bookinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lroch.bookinventory.data.InventoryContract.InventoryEntry;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;
    int quantity = 0;
    private EditText mTitleEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mPhoneEditText;
    private EditText mEmailEditText;
    private boolean mBookHasChanged = false;
    private Uri mCurrentBookUri;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Increase Quantity of books button
        final Button quantityUpBtn = findViewById(R.id.plus_quantity_btn);
        quantityUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityUp();
            }
        });
        //Decrease quantity of books button
        final Button quantityDownBtn = findViewById(R.id.minus_quantity_btn);
        quantityDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityDown();
            }
        });

        //Place order via phone
        ImageButton order = findViewById(R.id.phoneOrderBtn);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle(R.string.place_order);
                if (TextUtils.isEmpty(mPhoneEditText.getText().toString())) {
                    noPhoneNumberConfirmationDialog();
                } else {
                    openPhone();
                }
            }
        });

        //Place order via email
        ImageButton email = findViewById(R.id.emailBtn);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle(R.string.place_order);
                if (TextUtils.isEmpty(mEmailEditText.getText().toString())) {
                    noEmailConfirmationDialog();
                } else {
                    openEmail();
                }
            }
        });

        //Get the intent from the Catalog activity
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        //If the intent does not contain a book content URI, then we know we are creating
        // a new book
        if (mCurrentBookUri == null) {
            //This is a new book, so change the app bar to say "Add a book"
            setTitle(R.string.add_book);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
            email.setVisibility(View.GONE);
            order.setVisibility(View.GONE);
        } else {
            //This is an existing book, so change app bar to say "Edit book"
            setTitle(R.string.edit_book);
            //Start the loader
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mTitleEditText = findViewById(R.id.edit_book_title);
        mSupplierEditText = findViewById(R.id.edit_book_supplier);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_quantity_text);
        mPhoneEditText = findViewById(R.id.edit_book_phone);
        mEmailEditText = findViewById(R.id.edit_book_email);

        mTitleEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    public void quantityUp() {
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    public void quantityDown() {
        quantity = quantity - 1;
        displayQuantity(quantity);
    }

    public void displayQuantity(int quantity) {
        TextView quantityView = findViewById(R.id.edit_quantity_text);
        quantityView.setText(String.valueOf(quantity));
        if (quantity <= 0) {
            quantityView.setText("0");
            makeText(this, R.string.inventory_cant_less_than, LENGTH_SHORT).show();
        }
    }

    private void saveBook() {

        String titleString = mTitleEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();


        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(phoneString) && TextUtils.isEmpty(emailString)) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;}

            //Validate the following information has been provided
            if(titleString.isEmpty()){
                Toast.makeText(this, R.string.title_needed, Toast.LENGTH_SHORT).show();
                return;
            }
            if(supplierString.isEmpty()){
                Toast.makeText(this,R.string.supplier_needed,LENGTH_SHORT).show();
                return;
            }
            if(priceString.isEmpty()){
                Toast.makeText(this,R.string.price_needed,LENGTH_SHORT).show();
                return;
            }
            if(quantityString.isEmpty()){
                Toast.makeText(this,R.string.quantity_needed,LENGTH_SHORT).show();
                return;
            }
            if(phoneString.isEmpty() || emailString.isEmpty()){
                Toast.makeText(this,R.string.order_contact_needed,Toast.LENGTH_LONG).show();
                return;
            }

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }

        int quantity = 0;
        if (!TextUtils.isEmpty((quantityString))){
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PHONE, phoneString);
        values.put(InventoryEntry.COLUMN_PRODUCT_EMAIL, emailString);

        if (mCurrentBookUri == null) {

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                makeText(this, R.string.error_saving_book, LENGTH_SHORT).show();
            } else {
                makeText(this, R.string.successfully_saved, LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            if (rowsAffected == 0) {
                makeText(this, R.string.error_updating_book,
                        LENGTH_SHORT).show();
            } else {
                makeText(this, R.string.book_updated,
                        LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save book to the database
                saveBook();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the books table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_PHONE,
                InventoryEntry.COLUMN_PRODUCT_EMAIL
        };
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String email = cursor.getString(emailColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPhoneEditText.setText(phone);
            mEmailEditText.setText(email);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
    }

    /**
     * * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Action taken when user presses back arrow.
     */

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
    /**
     * Action taken when user places order via phone.
     * Source Phone call intent:
     * https://stackoverflow.com/questions/4275678/how-to-make-a-phone-call-using-intent-in-android
     */
    private void openPhone() {
        String callPhone = mPhoneEditText.getText().toString();
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(mCurrentBookUri.parse("tel:" + callPhone));
        startActivity(phoneIntent);
    }

    /**
     * Action taken when user places order via email.
     */
    private void openEmail() {
        String emailOrder = mEmailEditText.getText().toString();
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(mCurrentBookUri.parse("mailto:" + emailOrder)).setAction(null);
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }
    }

    /**
     * Provide warning for missing phone number of supplier when placing an order.
     */
    private void noPhoneNumberConfirmationDialog() {
        AlertDialog.Builder noPhone = new AlertDialog.Builder(this);
        noPhone.setMessage(R.string.phone_number_missing);
        noPhone.setPositiveButton(R.string.continue_selected, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //user clicked on "Continue" button so open phone dialer anyway.
                openPhone();
            }
        });
        noPhone.setNegativeButton(R.string.enter_phone_number, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }
        );
        AlertDialog alertDialog = noPhone.create();
        alertDialog.show();
    }
    /**
     * Provide warning for missing email of supplier when placing an order.
     */
    private void noEmailConfirmationDialog() {
        AlertDialog.Builder noEmail = new AlertDialog.Builder(this);
        noEmail.setMessage(R.string.email_missing);
        noEmail.setPositiveButton(R.string.continue_selected, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //user clicked on "Continue" button so open email anyway.
                openEmail();
            }
        });
        noEmail.setNegativeButton((R.string.add_email), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }
        );
        AlertDialog alertDialog = noEmail.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.k
        if (mCurrentBookUri != null) {

            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentbookUri
            // content URI already identifies the book that we want.

            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                makeText(this, getString(R.string.editor_delete_book_failed),
                        LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                makeText(this, getString(R.string.editor_delete_book_successful),
                        LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}