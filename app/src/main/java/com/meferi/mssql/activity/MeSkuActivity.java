package com.meferi.mssql.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.meferi.mssql.R;
import com.meferi.mssql.bean.ProductBean;
import com.meferi.mssql.task.LoginTask;
import com.meferi.mssql.tool.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MeSkuActivity extends AppCompatActivity {

    private static final String TAG = "SkuActivity";

    private Context mContext;
    private ImageView image;
    private TextView tvBarCode, tvInfo, tvPrice, tvProductName, tvWeight;

    private static final int MSG_DELAY_CLOSE = 1101;

    private ProductBean productBean;

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
                new LoginTask(new LoginTask.OnLoginListener() {
                    @Override
                    public void onLoginSuccess(String token) {
                        // 保存token供后续接口使用，比如使用SharedPreferences
                        Log.d("Login", "Login success with token: " + token);
                        // 执行下一步：请求商品数据
                        new GetSkuTask().execute(productBean.getBarcode());

                    }

                    @Override
                    public void onLoginFailed() {
                        Toast.makeText(MeSkuActivity.this, "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
                    }
                }).execute("http://sku.meferi.com:29999/api/user/admin/login", "root", "aidlux123");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        applyWallpaperBackground();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideNotchAndFullscreen();
        setContentView(R.layout.activity_product);
        mContext = this;

        productBean = getIntent().getParcelableExtra("product_key");
        if (productBean == null) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initBroadcast();

        new LoginTask(new LoginTask.OnLoginListener() {
            @Override
            public void onLoginSuccess(String token) {
                // 保存token供后续接口使用，比如使用SharedPreferences
                Log.d("Login", "Login success with token: " + token);
                // 执行下一步：请求商品数据
                new GetSkuTask().execute(productBean.getBarcode());

            }

            @Override
            public void onLoginFailed() {
                Toast.makeText(MeSkuActivity.this, "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
            }
        }).execute("http://sku.meferi.com:29999/api/user/admin/login", "root", "aidlux123");

        mHandler.sendEmptyMessageDelayed(MSG_DELAY_CLOSE, Constants.PRODUCT_WAIT_TIMEOUT);
    }

    private void initViews() {
        tvPrice = findViewById(R.id.tv_price);
        tvProductName = findViewById(R.id.tv_product_name);
        tvWeight = findViewById(R.id.tv_weight);
        tvBarCode = findViewById(R.id.tv_bar_code);
        tvInfo = findViewById(R.id.tv_info);
        image = findViewById(R.id.image);
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter("com.meferi.action.CMD.QUICKSCAN.RESULT");
        registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void hideNotchAndFullscreen() {
        supportRequestWindowFeature(1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private class GetSkuTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params == null || params.length == 0) return null;
            String barcode = params[0];
            String urlStr = "http://sku.meferi.com:29999/api/sku/info/" + barcode;

            try {
                Log.d("HttpGet", "Request URL: " + urlStr);

                HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                Log.d("HttpGet", "Response Code: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                connection.disconnect();

                Log.d("HttpGet", "Response Body: " + result.toString());

                return result.toString();
            } catch (Exception e) {
                Log.e("HttpGet", "Request failed: " + e.getMessage(), e);
                return null;
            }

        }

        @Override
        protected void onPostExecute(String json) {
            if (json != null) {
                parseJson(json);
            }
        }

        private void parseJson(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject data = jsonObject.getJSONObject("data");

                String name = data.getString("name");
                String barCode = data.getString("bar_code");
                String price = data.getString("price");
                String weight = data.getString("weight") + " Kg";
                String info = data.getString("info");
                JSONArray images = data.getJSONArray("images");
                String imageUrl = images.length() > 0 ? images.getJSONObject(0).getString("path") : "";

                updateUI(name, barCode, price, weight, info, imageUrl);

            } catch (JSONException e) {
                try {
                    JSONObject errorObj = new JSONObject(json);
                    Toast.makeText(mContext, errorObj.optString("message", "解析错误"), Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void updateUI(String name, String barcode, String price, String weight, String info, String imageUrl) {
            tvProductName.setText(name);
            tvBarCode.setText(barcode);
            tvPrice.setText(convertAndFormat(price));
            tvWeight.setText(weight);
            tvInfo.setText(info);

            Glide.with(mContext)
                    .load(imageUrl)
                    .override(480, 480)
                    .placeholder(R.drawable.loading)
                    .centerCrop()
                    .into(image);
        }
    }
    private void applyWallpaperBackground() {
        File wallpaperFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "wallpaper_product.jpg");
        Log.d(TAG, "wallpaper_product: " + wallpaperFile.getAbsolutePath());
        if (wallpaperFile.exists()) {
            LinearLayout llProduct = findViewById(R.id.ll_product);
            Bitmap bitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());

            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                llProduct.setBackground(drawable);
            }
        }

    }
    public String convertAndFormat(String input) {
        try {
            float number = Float.parseFloat(input);
            return String.format("%.2f", number);
        } catch (NumberFormatException e) {
            return input;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
