package com.meferi.mssql.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.meferi.mssql.tool.Constants;
import com.meferi.mssql.R;

public class DbSettingsActivity extends AppCompatActivity {

    private EditText editProductName, editProductPrice, editProductImage, editUnitPrice, editBarcode;

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_settings);
        getSupportActionBar().hide();
        hideSystemUI();

        editProductName = findViewById(R.id.editProductName);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductImage = findViewById(R.id.editProductImage);
        editUnitPrice = findViewById(R.id.editUnitPrice);
        editBarcode = findViewById(R.id.editBarcode);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);



        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        editProductName.setText(prefs.getString(Constants.KEY_PRODUCT_NAME, ""));
        editProductPrice.setText(prefs.getString(Constants.KEY_PRODUCT_PRICE, ""));
        editProductImage.setText(prefs.getString(Constants.KEY_PRODUCT_IMAGE, ""));
        editUnitPrice.setText(prefs.getString(Constants.KEY_UNIT_PRICE, ""));
        editBarcode.setText(prefs.getString(Constants.KEY_BARCODE, ""));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.KEY_PRODUCT_NAME, editProductName.getText().toString().trim());
            editor.putString(Constants.KEY_PRODUCT_PRICE, editProductPrice.getText().toString().trim());
            editor.putString(Constants.KEY_PRODUCT_IMAGE, editProductImage.getText().toString().trim());
            editor.putString(Constants.KEY_UNIT_PRICE, editUnitPrice.getText().toString().trim());
            editor.putString(Constants.KEY_BARCODE, editBarcode.getText().toString().trim());
            editor.apply();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
