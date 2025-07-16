package com.meferi.mssql.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.meferi.mssql.R;
import com.meferi.mssql.bean.ProductBean;
import com.meferi.mssql.db.ConfigManager;
import com.meferi.mssql.tool.Constants;
import com.meferi.mssql.tool.Utils;
import com.meferi.sdk.ParameterID;
import com.meferi.sdk.ScanManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private ScanManager mScannerManager;
    private boolean isPaused = false;
    private ProgressDialog progressDialog;
    private String ACTION_SEND_RESULT = "android.intent.action.MEF_ACTION";
    private String EXTRA_SCAN_BARCODE = "android.intent.action.MEF_DATA1";
    private int pendingDownloads = 0;

    // 初始化壁纸相关变量
    private String Productbarcode, ProductName, ProductPrice, ProductImage, UnitPrice;
    private String ip, user, password, database, table, port;

    // 扫描广播接收器
    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ACTION_SEND_RESULT.equals(intent.getAction())) return;
            ConfigManager configManager = new ConfigManager(MainActivity.this);

            String val = intent.getStringExtra(EXTRA_SCAN_BARCODE);
            if(val != null && val.equals("*#*#7895123#*#*")){
               Intent pintent = new Intent(MainActivity.this, MenuActivity.class);
               startActivity(pintent);
               return;
            } else if (Utils.isValidConfig(val)) {
                showProgressDialog("配置导入中...");

                new Thread(() -> {
                    boolean success = Utils.importConfig(MainActivity.this, val);

                    runOnUiThreadSafe(() -> {
                        dismissProgressDialog();

                        if (success) {
                            Toast.makeText(MainActivity.this, "配置导入成功", Toast.LENGTH_SHORT).show();

                            String homepageUrl = configManager.getConfig(Constants.KEY_HOMEPAGE_WALLPAPER_URL, "");
                            String productUrl = configManager.getConfig(Constants.KEY_PRODUCT_WALLPAPER_URL, "");

                            if (!homepageUrl.isEmpty() || !productUrl.isEmpty()) {
                                downloadWallpapers(homepageUrl, productUrl);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "配置导入失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();

                return;
            }

            // 普通条码流程
            ProductBean mProductBean = new ProductBean();
            mProductBean.setBarcode(val);
            boolean IsLogin = Utils.getBoolean("IsLogin", true);
            if (!isPaused) {
                Utils.putBoolean("IsLogin", false);
                boolean sku = Boolean.parseBoolean(configManager.getConfig(Constants.KEY_USE_MEFERI_SKU, "true"));
                Intent pintent = null;
                Log.d(TAG, "sku="+sku);
                if(sku){
                    pintent = new Intent(MainActivity.this, MeSkuActivity.class);
                    pintent.putExtra("product_key", mProductBean);

                }else {
                    pintent = new Intent(MainActivity.this, ProductActivity.class);
                    pintent.putExtra("product_key", mProductBean);
                }
                startActivity(pintent);
            } else {
                Intent quickScanResult = new Intent("com.meferi.action.CMD.QUICKSCAN.RESULT");
                quickScanResult.putExtra("barcode", val);
                sendBroadcast(quickScanResult);
            }
        }
    };

    // 壁纸下载入口
    private void downloadWallpapers(String homepageUrl, String productUrl) {
        int total = 0;
        if (!homepageUrl.isEmpty()) total++;
        if (!productUrl.isEmpty()) total++;

        pendingDownloads = total;
        ProgressDialog dialog = showProgressDialogWithBar("下载中", "正在下载壁纸...", total);

        if (!homepageUrl.isEmpty()) {
            downloadWallpaper(homepageUrl, "wallpaper_home.jpg", "主页壁纸", dialog);
        }

        if (!productUrl.isEmpty()) {
            downloadWallpaper(productUrl, "wallpaper_product.jpg", "产品壁纸", dialog);
        }
    }

    private void downloadWallpaper(String urlStr, String fileName, String label, ProgressDialog dialog) {
        new Thread(() -> {
            boolean success = false;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error " + conn.getResponseCode());
                }

                int length = conn.getContentLength();
                InputStream in = new BufferedInputStream(conn.getInputStream());

                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File tmpFile = new File(dir, fileName + ".tmp");
                File finalFile = new File(dir, fileName);

                OutputStream out = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[4096];
                int count;
                long total = 0;

                while ((count = in.read(buffer)) != -1) {
                    total += count;
                    out.write(buffer, 0, count);
                    if (length > 0) {
                        int progress = (int) (total * 100 / length);
                        runOnUiThreadSafe(() -> dialog.setProgress(progress));
                    }
                }

                out.flush();
                in.close();
                out.close();
                conn.disconnect();

                if (finalFile.exists()) finalFile.delete();
                success = tmpFile.renameTo(finalFile);

            } catch (Exception e) {
                Log.e(TAG, label + " 下载失败", e);
            }

            boolean finalSuccess = success;
            runOnUiThreadSafe(() -> onDownloadFinished(label, finalSuccess, dialog));
        }).start();
    }

    private void onDownloadFinished(String label, boolean success, ProgressDialog dialog) {
        pendingDownloads--;
        if (pendingDownloads <= 0) {
            dialog.dismiss();
            String msg = success ? "壁纸下载完成" : "部分下载失败";
            File wallpaperFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "wallpaper_home.jpg");
            if (wallpaperFile.exists()) {
                ImageView bgView = findViewById(R.id.iv_background);
                Glide.with(this).load(wallpaperFile).into(bgView);
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    // ========= ProgressDialog 显示辅助方法 =========
    private void showProgressDialog(String msg) {
        runOnUiThreadSafe(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(false);
            progressDialog.show();
        });
    }

    private void dismissProgressDialog() {
        runOnUiThreadSafe(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    private ProgressDialog showProgressDialogWithBar(String title, String message, int max) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(title);
        pd.setMessage(message);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(max);
        pd.setCancelable(false);
        pd.show();
        return pd;
    }

    private void runOnUiThreadSafe(Runnable action) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(action);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.init(this);

        ConfigManager config = new ConfigManager(this);
        Productbarcode = config.getConfig(Constants.KEY_BARCODE, Constants.DEFAULT_BARCODE);
        ProductName = config.getConfig(Constants.KEY_PRODUCT_NAME, Constants.DEFAULT_PRODUCT_NAME);
        ProductPrice = config.getConfig(Constants.KEY_PRODUCT_PRICE, Constants.DEFAULT_PRODUCT_PRICE);
        ProductImage = config.getConfig(Constants.KEY_PRODUCT_IMAGE, Constants.DEFAULT_PRODUCT_IMAGE);
        UnitPrice = config.getConfig(Constants.KEY_UNIT_PRICE, Constants.DEFAULT_UNIT_PRICE);
        ip = config.getConfig(Constants.KEY_API_ADDRESS, Constants.DEFAULT_API_ADDRESS);
        user = config.getConfig(Constants.KEY_USERNAME, Constants.DEFAULT_USERNAME);
        password = config.getConfig(Constants.KEY_PASSWORD, Constants.DEFAULT_PASSWORD);
        database = config.getConfig(Constants.KEY_DATABASE_NAME, Constants.DEFAULT_DATABASE_NAME);
        table = config.getConfig(Constants.KEY_TABLE_NAME, Constants.DEFAULT_TABLE_NAME);
        port = config.getConfig(Constants.KEY_PORT, Constants.DEFAULT_PORT);

        File wallpaperFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "wallpaper_home.jpg");
        if (wallpaperFile.exists()) {
            ImageView bgView = findViewById(R.id.iv_background);
            Glide.with(this).load(wallpaperFile).into(bgView);
        }

        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        hideSystemUI();

        findViewById(R.id.btn_settings).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MenuActivity.class)));


        mScannerManager = new ScanManager();
        try {
            ACTION_SEND_RESULT = mScannerManager.getParameterString(ParameterID.BROADCAST_ACTION);
            EXTRA_SCAN_BARCODE = mScannerManager.getParameterString(ParameterID.BROADCAST_DATA);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        registerReceiver(mResultReceiver, new IntentFilter(ACTION_SEND_RESULT), Context.RECEIVER_EXPORTED);



        findViewById(R.id.btn_settings).setVisibility(View.VISIBLE);

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mResultReceiver);
        } catch (Exception ignored) {
        }
    }
}
