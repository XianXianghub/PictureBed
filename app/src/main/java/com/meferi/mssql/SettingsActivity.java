

package com.meferi.mssql;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText editApiAddress, editUsername, editPassword, editDatabaseName, editPort, editTableName;
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
        setContentView(R.layout.activity_server_config); // 替换为你的布局名
        getSupportActionBar().hide();
        hideSystemUI();
        // 初始化 EditText
        editApiAddress = findViewById(R.id.editApiAddress);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editDatabaseName = findViewById(R.id.editDatabaseName);
        editPort = findViewById(R.id.editPort);
        editProductName = findViewById(R.id.editProductName);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductImage = findViewById(R.id.editProductImage);
        editUnitPrice = findViewById(R.id.editUnitPrice);
        editBarcode = findViewById(R.id.editBarcode);
        editTableName = findViewById(R.id.editTableName);

        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);

        // 加载保存的数据
        editApiAddress.setText(prefs.getString(Constants.KEY_API_ADDRESS, Constants.DEFAULT_API_ADDRESS));
        editUsername.setText(prefs.getString(Constants.KEY_USERNAME, Constants.DEFAULT_USERNAME));
        editPassword.setText(prefs.getString(Constants.KEY_PASSWORD, Constants.DEFAULT_PASSWORD));
        editDatabaseName.setText(prefs.getString(Constants.KEY_DATABASE_NAME, Constants.DEFAULT_DATABASE_NAME));
        editPort.setText(prefs.getString(Constants.KEY_PORT, Constants.DEFAULT_PORT));
        editProductName.setText(prefs.getString(Constants.KEY_PRODUCT_NAME, Constants.DEFAULT_PRODUCT_NAME));
        editProductPrice.setText(prefs.getString(Constants.KEY_PRODUCT_PRICE, Constants.DEFAULT_PRODUCT_PRICE));
        editProductImage.setText(prefs.getString(Constants.KEY_PRODUCT_IMAGE, Constants.DEFAULT_PRODUCT_IMAGE));
        editUnitPrice.setText(prefs.getString(Constants.KEY_UNIT_PRICE, Constants.DEFAULT_UNIT_PRICE));
        editBarcode.setText(prefs.getString(Constants.KEY_BARCODE, Constants.DEFAULT_BARCODE));
        editTableName.setText(prefs.getString(Constants.KEY_TABLE_NAME, Constants.DEFAULT_TABLE_NAME));
        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.KEY_API_ADDRESS, editApiAddress.getText().toString().trim());
            editor.putString(Constants.KEY_USERNAME, editUsername.getText().toString().trim());
            editor.putString(Constants.KEY_PASSWORD, editPassword.getText().toString().trim());
            editor.putString(Constants.KEY_DATABASE_NAME, editDatabaseName.getText().toString().trim());
            editor.putString(Constants.KEY_PORT, editPort.getText().toString().trim());
            editor.putString(Constants.KEY_PRODUCT_NAME, editProductName.getText().toString().trim());
            editor.putString(Constants.KEY_PRODUCT_PRICE, editProductPrice.getText().toString().trim());
            editor.putString(Constants.KEY_PRODUCT_IMAGE, editProductImage.getText().toString().trim());
            editor.putString(Constants.KEY_UNIT_PRICE, editUnitPrice.getText().toString().trim());
            editor.putString(Constants.KEY_BARCODE, editBarcode.getText().toString().trim());
            editor.putString(Constants.KEY_TABLE_NAME, editTableName.getText().toString().trim());
            editor.apply();
            finish(); // 保存后退出
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // 返回按钮的 ID 是 android.R.id.home
            finish(); // 关闭当前 Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

