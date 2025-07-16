package com.meferi.mssql.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.meferi.mssql.R;
import com.meferi.mssql.bean.ProductBean;
import com.meferi.mssql.db.ConfigManager;
import com.meferi.mssql.tool.Constants;

import java.io.File;
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

    // 配置字段名（从数据库中读取的字段名）
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
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_CLOSE, Constants.PRODUCT_WAIT_TIMEOUT);

                String barcode = intent.getStringExtra("barcode");
                productBean.setBarcode(barcode);

                String sql = "SELECT * FROM " + table + " WHERE [" + Productbarcode + "] = '" + productBean.getBarcode() + "'";
                Log.d(TAG, "execute sql=" + sql);
                connectToDatabase(sql);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        hideSystemUI();

        productBean = getIntent().getParcelableExtra("product_key");
        if (productBean == null) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        // 注册扫码结果广播接收器
        IntentFilter mFilter = new IntentFilter("com.meferi.action.CMD.QUICKSCAN.RESULT");
        registerReceiver(mReceiver, mFilter, Context.RECEIVER_EXPORTED);

        // 延时自动关闭界面
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_CLOSE, Constants.PRODUCT_WAIT_TIMEOUT);
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

        initConfigs();

        String sql = "SELECT * FROM " + table + " WHERE [" + Productbarcode + "] = '" + productBean.getBarcode() + "'";
        Log.d(TAG, "execute sql=" + sql);

        applyWallpaperBackground();

        connectToDatabase(sql);
    }

    private void initConfigs() {
        ConfigManager configManager = new ConfigManager(this);

        Productbarcode = configManager.getConfig(Constants.KEY_BARCODE, "");
        ProductName = configManager.getConfig(Constants.KEY_PRODUCT_NAME, "");
        ProductPrice = configManager.getConfig(Constants.KEY_PRODUCT_PRICE, "");
        ProductImage = configManager.getConfig(Constants.KEY_PRODUCT_IMAGE, "");
        UnitPrice = configManager.getConfig(Constants.KEY_UNIT_PRICE, "");

        ip = configManager.getConfig(Constants.KEY_API_ADDRESS, "");
        user = configManager.getConfig(Constants.KEY_USERNAME, "");
        password = configManager.getConfig(Constants.KEY_PASSWORD, "");
        database = configManager.getConfig(Constants.KEY_DATABASE_NAME, "");
        table = configManager.getConfig(Constants.KEY_TABLE_NAME, "");
        port = configManager.getConfig(Constants.KEY_PORT, "1433");
        boolean debug = Boolean.parseBoolean(configManager.getConfig(Constants.KEY_DEBUG_MODE, "false"));
        tvInfo.setVisibility(debug?View.VISIBLE:View.GONE);
    }

    private void applyWallpaperBackground() {
        File wallpaperFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "wallpaper_product.jpg");
        Log.d(TAG, "wallpaperFile: " + wallpaperFile.getAbsolutePath());

        if (wallpaperFile.exists()) {
            LinearLayout llProduct = findViewById(R.id.ll_product);
            Bitmap bitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());

            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                llProduct.setBackground(drawable);
            }
        }
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

                Log.d(TAG, "hasResultSet=" + hasResultSet);
                isExistData = false;
                StringBuilder resultBuilder = new StringBuilder();

                if (hasResultSet) {
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    Log.d(TAG, "columnCount=" + columnCount);

                    while (rs.next()) {
                        isExistData = true;
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            String columnValue = rs.getString(i);
                            Log.d(TAG, "columnName=" + columnName + " columnValue=" + columnValue);

                            // 这里判断列名是否和配置字段名相等，区分大小写根据数据库而定
                            if (columnName.equalsIgnoreCase(ProductName)) productBean.setName(columnValue);
                            if (columnName.equalsIgnoreCase(ProductPrice)) productBean.setPrice(columnValue);
                            if (columnName.equalsIgnoreCase(ProductImage)) productBean.setImg(columnValue);
                            if (columnName.equalsIgnoreCase(UnitPrice)) productBean.setPriceunt(columnValue);

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
                        Toast.makeText(this, "未找到该商品信息", Toast.LENGTH_SHORT).show();
                    } else {
                        showProduct();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Database error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "数据库连接或查询异常：" + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (Exception e) {
                    Log.e(TAG, "关闭数据库连接异常", e);
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
                .override(480, 480)
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
