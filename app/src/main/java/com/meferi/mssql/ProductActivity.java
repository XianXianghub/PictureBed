package com.meferi.mssql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "ProductActivity";
    private static final int MSG_DELAY_CLOSE = 1101;

    private ProductBean productBean;

    private TextView tvPrice, tvProductName, tvWeight, tvBarCode, tvInfo;
    private ImageView image;

    private String Productbarcode, ProductName, ProductPrice, ProductImage, UnitPrice;
    private String ip, user, password, database, table, port;

    private boolean isExistData = false;

    private final Handler mHandler = new Handler(msg -> {
        if (msg.what == MSG_DELAY_CLOSE) {
            finish();
        }
        return true;
    });

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.meferi.action.CMD.QUICKSCAN.RESULT".equals(intent.getAction())) {
                mHandler.removeMessages(MSG_DELAY_CLOSE);
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_CLOSE, 10000L);

                String barcode = intent.getStringExtra("barcode");
                productBean.setBarcode(barcode);
                String sql = "SELECT * FROM " + table + " WHERE " + Productbarcode + " = '" + productBean.getBarcode() + "'";
                connectToDatabase(sql);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        hideSystemUI();

        productBean = getIntent().getParcelableExtra("product_key");
        if (productBean == null) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_CLOSE, 10000L);

        IntentFilter mFilter = new IntentFilter("com.meferi.action.CMD.QUICKSCAN.RESULT");
        registerReceiver(mReceiver, mFilter, Context.RECEIVER_EXPORTED);
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
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        Productbarcode = prefs.getString(Constants.KEY_BARCODE, "");
        ProductName = prefs.getString(Constants.KEY_PRODUCT_NAME, "");
        ProductPrice = prefs.getString(Constants.KEY_PRODUCT_PRICE, "");
        ProductImage = prefs.getString(Constants.KEY_PRODUCT_IMAGE, "");
        UnitPrice = prefs.getString(Constants.KEY_UNIT_PRICE, "");

        ip = prefs.getString(Constants.KEY_API_ADDRESS, "");
        user = prefs.getString(Constants.KEY_USERNAME, "");
        password = prefs.getString(Constants.KEY_PASSWORD, "");
        database = prefs.getString(Constants.KEY_DATABASE_NAME, "");
        table = prefs.getString(Constants.KEY_TABLE_NAME, "");
        port = prefs.getString(Constants.KEY_PORT, "1433");

        String sql = "SELECT * FROM " + table + " WHERE " + Productbarcode + " = '" + productBean.getBarcode() + "'";
        connectToDatabase(sql);
    }

    private void connectToDatabase(String sql) {
        if (sql == null || sql.isEmpty()) return;

        new Thread(() -> {
            Connection connection = null;
            Statement stmt = null;
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database +
                        ";user=" + user + ";password=" + password + ";";
                connection = DriverManager.getConnection(url);
                stmt = connection.createStatement();
                boolean hasResultSet = stmt.execute(sql);

                isExistData = false;
                StringBuilder resultBuilder = new StringBuilder();

                if (hasResultSet) {
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    while (rs.next()) {
                        isExistData = true;
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            String columnValue = rs.getString(i);

                            if (columnName.equals(ProductName)) productBean.setName(columnValue);
                            if (columnName.equals(ProductPrice)) productBean.setPrice(columnValue);
                            if (columnName.equals(ProductImage)) productBean.setImg(columnValue);
                            if (columnName.equals(UnitPrice)) productBean.setPriceunt(columnValue);

                            resultBuilder.append(columnName).append("=").append(columnValue);
                            if (i < columnCount) resultBuilder.append(", ");
                        }
                        resultBuilder.append("\n");
                    }
                    rs.close();
                }

                productBean.setDbinfo(resultBuilder.toString());

                runOnUiThread(() -> {
                    if (!isExistData) {
                        Toast.makeText(this, "No product found.", Toast.LENGTH_SHORT).show();
                    } else {
                        showProduct();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Database error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (Exception e) {
                    Log.e(TAG, "Close connection error", e);
                }
            }
        }).start();
    }

    private void initViews() {
        tvPrice = findViewById(R.id.tv_price);
        tvProductName = findViewById(R.id.tv_product_name);
        tvWeight = findViewById(R.id.tv_weight);
        tvBarCode = findViewById(R.id.tv_bar_code);
        tvInfo = findViewById(R.id.tv_info);
        image = findViewById(R.id.image);
    }

    private void showProduct() {
        tvPrice.setText(productBean.getPrice());
        tvProductName.setText(productBean.getName());
        tvWeight.setText(productBean.getPriceunt());
        tvBarCode.setText(productBean.getBarcode());
        tvInfo.setText(productBean.getDbinfo());

        Glide.with(this)
                .load(productBean.getImg())
                .override(480, 480) // 限制图片尺寸
                .placeholder(R.drawable.loading)
                .centerCrop()
                .into(image);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
