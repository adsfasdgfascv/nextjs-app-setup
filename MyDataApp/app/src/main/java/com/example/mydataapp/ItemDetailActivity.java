package com.example.mydataapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription;
    private Button buttonSave, buttonDelete;

    private DatabaseHelper dbHelper;
    private int itemId = -1;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            itemId = intent.getIntExtra("itemId", -1);
            userId = intent.getIntExtra("userId", -1);
        }

        if (itemId != -1) {
            loadItemDetails();
            buttonDelete.setVisibility(View.VISIBLE);
        } else {
            buttonDelete.setVisibility(View.GONE);
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem();
            }
        });
    }

    private void loadItemDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ITEMS,
                null,
                DatabaseHelper.COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_DESCRIPTION));
            editTextTitle.setText(title);
            editTextDescription.setText(description);
            cursor.close();
        }
    }

    private void saveItem() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_TITLE, title);
        values.put(DatabaseHelper.COLUMN_ITEM_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_ITEM_USER_ID, userId);

        if (itemId == -1) {
            // Insert new item
            long newRowId = db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
            if (newRowId != -1) {
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update existing item
            int rowsAffected = db.update(DatabaseHelper.TABLE_ITEMS, values,
                    DatabaseHelper.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
            if (rowsAffected > 0) {
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteItem() {
        if (itemId != -1) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete(DatabaseHelper.TABLE_ITEMS,
                    DatabaseHelper.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
