package com.meferi.mssql.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meferi.mssql.tool.Constants;
import com.meferi.mssql.R;
import com.meferi.mssql.db.ConfigManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends AppCompatActivity {

    private EditText homepageAddress, productAddress;
    private ConfigManager configManager;

    // 计数器，记录剩余待下载的壁纸数量
    private int pendingDownloads = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_settings);
        getSupportActionBar().hide();
        hideSystemUI();

        homepageAddress = findViewById(R.id.homepage_address);
        productAddress = findViewById(R.id.edit_product_address);
        Switch switchDebug = findViewById(R.id.switch_debug);
        Switch switchSku = findViewById(R.id.switch_sku);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> finish());

        configManager = new ConfigManager(this);
        switchDebug.setChecked(Boolean.parseBoolean(configManager.getConfig(Constants.KEY_DEBUG_MODE, "false")));
        switchSku.setChecked(Boolean.parseBoolean(configManager.getConfig(Constants.KEY_USE_MEFERI_SKU, "true")));
        switchDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.putConfig(Constants.KEY_DEBUG_MODE, Boolean.toString(isChecked));
        });

        switchSku.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.putConfig(Constants.KEY_USE_MEFERI_SKU, Boolean.toString(isChecked));
        });

        homepageAddress.setText(configManager.getConfig(Constants.KEY_HOMEPAGE_WALLPAPER_URL, ""));
        productAddress.setText(configManager.getConfig(Constants.KEY_PRODUCT_WALLPAPER_URL, ""));

        btnSave.setOnClickListener(v -> {
            String homepageUrl = homepageAddress.getText().toString().trim();
            String productUrl = productAddress.getText().toString().trim();

            configManager.putConfig(Constants.KEY_HOMEPAGE_WALLPAPER_URL, homepageUrl);
            configManager.putConfig(Constants.KEY_PRODUCT_WALLPAPER_URL, productUrl);

            if (homepageUrl.isEmpty() && productUrl.isEmpty()) {
                Toast.makeText(this, getString(R.string.url_cannot_be_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            pendingDownloads = 0;
            if (!homepageUrl.isEmpty()) pendingDownloads++;
            if (!productUrl.isEmpty()) pendingDownloads++;

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.downloading));
            progressDialog.setMessage(getString(R.string.downloading_wallpapers));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

            if (!homepageUrl.isEmpty()) {
                downloadWallpaper(homepageUrl, "wallpaper_home.jpg", getString(R.string.homepage_wallpaper), progressDialog);
            }

            if (!productUrl.isEmpty()) {
                downloadWallpaper(productUrl, "wallpaper_product.jpg", getString(R.string.product_wallpaper), progressDialog);
            }
        });

    }

    private synchronized void onDownloadFinished(String label, boolean success, ProgressDialog progressDialog) {
        pendingDownloads--;

        // 关闭进度条在最后一个下载结束后
        if (pendingDownloads <= 0) {
            progressDialog.dismiss();

            String msg = success ? getString(R.string.all_wallpapers_downloaded) : getString(R.string.some_wallpapers_failed);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void downloadWallpaper(String imageUrl, String fileName, String label, ProgressDialog progressDialog) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            OutputStream output = null;
            boolean success = false;

            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception(getString(R.string.http_error_code) + connection.getResponseCode());
                }

                int contentLength = connection.getContentLength();
                input = new BufferedInputStream(connection.getInputStream());

                File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File tempFile = new File(downloadsDir, fileName + ".tmp");
                if (tempFile.exists()) tempFile.delete();
                File finalFile = new File(downloadsDir, fileName);

                output = new FileOutputStream(tempFile);

                byte[] buffer = new byte[4096];
                int count;
                long total = 0;

                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (contentLength > 0) {
                        int progress = (int) (total * 100 / contentLength);
                        runOnUiThread(() -> progressDialog.setProgress(progress));
                    }
                    output.write(buffer, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                connection.disconnect();

                if (finalFile.exists()) finalFile.delete();
                boolean renamed = tempFile.renameTo(finalFile);
                success = renamed;

            } catch (Exception e) {
                Log.e("WallpaperDownload", label + " " + getString(R.string.download_failed), e);
            } finally {
                try {
                    if (input != null) input.close();
                    if (output != null) output.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e("WallpaperDownload", getString(R.string.close_stream_error), e);
                }
                boolean finalSuccess = success;
                runOnUiThread(() -> onDownloadFinished(label, finalSuccess, progressDialog));
            }
        }).start();
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
}
