package com.meferi.mssql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.meferi.sdk.ParameterID;
import com.meferi.sdk.ScanManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;


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
    private Product mProduct;
    private boolean isExistData = false;


    private ImageView itemImage;
    private TextView itemName, itemCurrentPrice, itemPriceUnit;


    private TextView textResult;


    // Action and extra keys for the broadcast
    private String ACTION_SEND_RESULT = "android.intent.action.MEF_ACTION";
    private String EXTRA_SCAN_BARCODE = "android.intent.action.MEF_DATA1";

    // BroadcastReceiver to handle scan results
    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SEND_RESULT.equals(action)) {
                // Get the scanned barcode from the intent
                String val = intent.getStringExtra(EXTRA_SCAN_BARCODE);

                Bundle bundle = intent.getExtras();
                for (String key: bundle.keySet())
                {
                    Log.i(TAG, "Key=" + key + ", content=" +bundle.getString(key));
                }
                String sql = "SELECT * FROM "+table+" WHERE "+Productbarcode+" = '"+val+"'";
                connectToDatabase(sql);
            }
        }
    };

    private final int[] rgbColors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE
    };
    private int colorIndex = 0;


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

        itemImage.setImageDrawable(null);
        itemImage.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_background));

    }

    @Override
    protected void onResume() {
        super.onResume();
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
        setContentView(R.layout.activity_home);

        textResult = findViewById(R.id.textResult);



        itemImage = findViewById(R.id.itemImage);
        itemName = findViewById(R.id.itemName);
        itemCurrentPrice = findViewById(R.id.itemCurrentPrice);
        itemPriceUnit = findViewById(R.id.itemPriceUnit);

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

    private void connectToDatabase(String sql) {

        if (sql.isEmpty()) {
            runOnUiThread(() -> textResult.setText("Please enter an SQL query."));
            return;
        }


        itemImage.setImageDrawable(null);
        itemImage.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_background));

        isExistData = false;

        new Thread(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database +
                        ";user=" + user + ";password=" + password + ";";

                Log.d("ddd", "url="+url);
                Connection connection = DriverManager.getConnection(url);
                Statement stmt = connection.createStatement();
                boolean hasResultSet = stmt.execute(sql);

                StringBuilder resultBuilder = new StringBuilder();
                mProduct = new Product();

                if (hasResultSet) {
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();


//                    private String barcode ;
//                    private String ProductName ;
//                    private String ProductPrice ;
//                    private String ProductImage ;
//                    private String UnitPrice ;
                    Log.d(TAG, "ProductName=" + ProductName);
                    Log.d(TAG, "ProductPrice=" + ProductPrice);
                    Log.d(TAG, "ProductImage=" + ProductImage);
                    Log.d(TAG, "UnitPrice=" + UnitPrice);
                    Log.d(TAG, "Productbarcode=" + Productbarcode);
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            isExistData = true;
                            String columnName = metaData.getColumnName(i);
                            String columnValue = rs.getString(i);
                            Log.d(TAG, "columnName=" + columnName+"  columnValue="+columnValue);

                            if(columnName.equals(ProductName)){
                                mProduct.setName(columnValue);
                            } else if(columnName.equals(ProductPrice)){
                                mProduct.setPrice(columnValue);
                            }else if(columnName.equals(ProductImage)){
                                mProduct.setImg(columnValue);
                            }else if(columnName.equals(UnitPrice)){
                                mProduct.setPriceunt(columnValue);
                            }else if(columnName.equals(Productbarcode)){
                                mProduct.setBarcode(columnValue);
                            }
                            resultBuilder.append(columnName).append("=").append(columnValue);
                            if (i < columnCount) resultBuilder.append(", ");
                        }
                        resultBuilder.append("\n");
                    }
                    rs.close();
                } else {
                    int updateCount = stmt.getUpdateCount();
                    resultBuilder.append("Update Count: ").append(updateCount);
                }
                Log.d(TAG, "isExistData=" + isExistData);

                runOnUiThread(() -> {
                    if(!isExistData) {
                        itemName.setText("Product name");
                    }else{
                        itemName.setText(mProduct.getName());
                        itemCurrentPrice.setText(mProduct.getPrice());
                        itemPriceUnit.setText(mProduct.getPriceunt());
                        Log.d(TAG, "mProduct=" + mProduct);


                        Glide.with(TestActivity.this)
                                .load(mProduct.getImg())
                                .placeholder(R.drawable.loading)
                                .into(itemImage);
                    }
                });



                stmt.close();
                connection.close();

                runOnUiThread(() -> {
                    textResult.setText(resultBuilder.toString());
                    textResult.setTextColor(rgbColors[colorIndex]);
                    colorIndex = (colorIndex + 1) % rgbColors.length;
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    textResult.setText("Error:\n" + e.getMessage());
                    textResult.setTextColor(Color.RED);
                });
            }
        }).start();
    }

    // 添加设置按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
