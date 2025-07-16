package com.meferi.mssql.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.meferi.mssql.tool.Constants;
import com.meferi.mssql.R;
import com.meferi.mssql.db.ConfigManager;

public class ServerSettingsActivity extends AppCompatActivity {

    private EditText editApiAddress, editUsername, editPassword, editDatabaseName, editPort, editTableName;

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
    private  ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);
        getSupportActionBar().hide();
        hideSystemUI();

        editApiAddress = findViewById(R.id.editApiAddress);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editDatabaseName = findViewById(R.id.editDatabaseName);
        editPort = findViewById(R.id.editPort);
        editTableName = findViewById(R.id.editTableName);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        configManager = new ConfigManager(this);

        editApiAddress.setText(configManager.getConfig(Constants.KEY_API_ADDRESS, ""));
        editUsername.setText(configManager.getConfig(Constants.KEY_USERNAME, ""));
        editPassword.setText(configManager.getConfig(Constants.KEY_PASSWORD, ""));
        editDatabaseName.setText(configManager.getConfig(Constants.KEY_DATABASE_NAME, ""));
        editPort.setText(configManager.getConfig(Constants.KEY_PORT, ""));
        editTableName.setText(configManager.getConfig(Constants.KEY_TABLE_NAME, ""));


        btnSave.setOnClickListener(v -> {
            configManager.putConfig(Constants.KEY_API_ADDRESS, editApiAddress.getText().toString().trim());
            configManager.putConfig(Constants.KEY_USERNAME, editUsername.getText().toString().trim());
            configManager.putConfig(Constants.KEY_PASSWORD, editPassword.getText().toString().trim());
            configManager.putConfig(Constants.KEY_DATABASE_NAME, editDatabaseName.getText().toString().trim());
            configManager.putConfig(Constants.KEY_PORT, editPort.getText().toString().trim());
            configManager.putConfig(Constants.KEY_TABLE_NAME, editTableName.getText().toString().trim());
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
