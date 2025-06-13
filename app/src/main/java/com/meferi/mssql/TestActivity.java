package com.meferi.mssql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.meferi.sdk.ParameterID;
import com.meferi.sdk.ScanManager;


public class TestActivity extends AppCompatActivity {

    private String TAG = TestActivity.class.getSimpleName();
    ScanManager mScannerManager;
    private String Productbarcode ;
    private String ProductName ;
    private String ProductPrice ;
    private String ProductImage ;
    private String UnitPrice ;
    private String ip;
    private String user;
    private String password;
    private String database;
    private String table;
    private String port;

    private boolean isPaused = false;





    private String ACTION_SEND_RESULT = "android.intent.action.MEF_ACTION";
    private String EXTRA_SCAN_BARCODE = "android.intent.action.MEF_DATA1";

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SEND_RESULT.equals(action)) {
                // Get the scanned barcode from the intent
                String val = intent.getStringExtra(EXTRA_SCAN_BARCODE);
                ProductBean mProductBean = new ProductBean();
                mProductBean.setBarcode(val);
                boolean IsLogin = Utils.getBoolean("IsLogin", true);
                Log.d("ProductActivity", "isPaused="+isPaused);
                if (!isPaused) {
                    Utils.putBoolean("IsLogin", false);
                    Intent pintent = new Intent(TestActivity.this, ProductActivity.class);
                    pintent.putExtra("product_key", mProductBean);
                    startActivity(pintent);
                    return;
                }
                Intent intent3 = new Intent();
                intent3.setAction("com.meferi.action.CMD.QUICKSCAN.RESULT");
                intent3.putExtra("barcode", val);
                TestActivity.this.sendBroadcast(intent3);

            }
        }
    };





    public  void initDefaults() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 检查并设置所有默认值
        setDefaultIfNotExists(prefs, editor, Constants.KEY_API_ADDRESS, Constants.DEFAULT_API_ADDRESS);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_USERNAME, Constants.DEFAULT_USERNAME);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_PASSWORD, Constants.DEFAULT_PASSWORD);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_DATABASE_NAME, Constants.DEFAULT_DATABASE_NAME);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_PORT, Constants.DEFAULT_PORT);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_PRODUCT_NAME, Constants.DEFAULT_PRODUCT_NAME);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_PRODUCT_PRICE, Constants.DEFAULT_PRODUCT_PRICE);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_PRODUCT_IMAGE, Constants.DEFAULT_PRODUCT_IMAGE);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_UNIT_PRICE, Constants.DEFAULT_UNIT_PRICE);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_BARCODE, Constants.DEFAULT_BARCODE);
        setDefaultIfNotExists(prefs, editor, Constants.KEY_TABLE_NAME, Constants.DEFAULT_TABLE_NAME);

        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.init(this);

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        Productbarcode =  prefs.getString(Constants.KEY_BARCODE, Constants.DEFAULT_BARCODE);
        ProductName =  prefs.getString(Constants.KEY_PRODUCT_NAME, Constants.DEFAULT_PRODUCT_NAME);
        ProductPrice=  prefs.getString(Constants.KEY_PRODUCT_PRICE, Constants.DEFAULT_PRODUCT_PRICE);
        ProductImage =  prefs.getString(Constants.KEY_PRODUCT_IMAGE, Constants.DEFAULT_PRODUCT_IMAGE);
        UnitPrice =  prefs.getString(Constants.KEY_UNIT_PRICE, Constants.DEFAULT_UNIT_PRICE);


        ip = prefs.getString(Constants.KEY_API_ADDRESS, Constants.DEFAULT_API_ADDRESS);
        user = prefs.getString(Constants.KEY_USERNAME, Constants.DEFAULT_USERNAME);
        password = prefs.getString(Constants.KEY_PASSWORD, Constants.DEFAULT_PASSWORD);
        database = prefs.getString(Constants.KEY_DATABASE_NAME, Constants.DEFAULT_DATABASE_NAME);
        table = prefs.getString(Constants.KEY_TABLE_NAME, Constants.DEFAULT_TABLE_NAME);
        port = prefs.getString(Constants.KEY_PORT, Constants.DEFAULT_PORT);

        // Product related variables
        Log.d(TAG, "Product Barcode: " + Productbarcode);
        Log.d(TAG, "Product Name: " + ProductName);
        Log.d(TAG, "Product Price: " + ProductPrice);
        Log.d(TAG, "Product Image: " + ProductImage);
        Log.d(TAG, "Unit Price: " + UnitPrice);

// API/connection related variables
        Log.d(TAG, "IP: " + ip);
        Log.d(TAG, "User: " + user);
        Log.d(TAG, "Password: " + password); // Be careful with logging passwords in production!
        Log.d(TAG, "Database: " + database);
        Log.d(TAG, "Table: " + table);
        Log.d(TAG, "Port: " + port);
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(5894);
        }
    }

    private static void setDefaultIfNotExists(SharedPreferences prefs, SharedPreferences.Editor editor,
                                              String key, String defaultValue) {
        if (!prefs.contains(key)) {
            editor.putString(key, defaultValue);
        }
    }
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
        setContentView(R.layout.activity_product_home);
        getSupportActionBar().hide();
        hideSystemUI();

        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TestActivity.this, SettingsActivity.class));
            }
        });


        // Initialize scanner manager
        mScannerManager = new ScanManager();
        try {
            // Get the action and data parameters for the broadcast from the scanner manager
            ACTION_SEND_RESULT = mScannerManager.getParameterString(ParameterID.BROADCAST_ACTION);
            EXTRA_SCAN_BARCODE = mScannerManager.getParameterString(ParameterID.BROADCAST_DATA);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        Log.d(TAG, "ACTION_SEND_RESULT=" + ACTION_SEND_RESULT + "  EXTRA_SCAN_BARCODE=" + EXTRA_SCAN_BARCODE);

        // Register the broadcast receiver
        IntentFilter intFilter = new IntentFilter();
        intFilter.addAction(ACTION_SEND_RESULT);
        registerReceiver(mResultReceiver, intFilter,Context.RECEIVER_EXPORTED);

        initDefaults();



    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Unregister the broadcast receiver
            unregisterReceiver(mResultReceiver);
        } catch (Exception e) {
            // Handle exception during receiver unregistration
        }
    }
}
