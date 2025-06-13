package com.meferi.mssql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import kotlin.jvm.internal.Intrinsics;

public class ProductActivity extends AppCompatActivity {
    private final int MSG_DELEY_CLOSE = 1101;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.meferi.snscannerproduct_demo.ProductActivity$mReceiver$1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Handler handler;
            Handler handler2;
            int i;
            Handler handler3;
            int i2;
            Intrinsics.checkNotNullParameter(context, "context");
            Intrinsics.checkNotNullParameter(intent, "intent");
            if (Intrinsics.areEqual(intent.getAction(), "com.meferi.action.CMD.QUICKSCAN")) {
                handler = ProductActivity.this.mHandler;
                if (handler != null) {
                    handler2 = ProductActivity.this.mHandler;
                    i = ProductActivity.this.MSG_DELEY_CLOSE;
                    handler2.removeMessages(i);
                    handler3 = ProductActivity.this.mHandler;
                    Intrinsics.checkNotNull(handler3);
                    i2 = ProductActivity.this.MSG_DELEY_CLOSE;
                    handler3.sendEmptyMessageDelayed(i2, 10000L);
                }
            }
        }
    };
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
    // Global views
    private TextView tvPrice, tvProductName, tvWeight, tvBarCode, tvInfo;
    private ImageView image;
    private final Handler mHandler = new Handler() { // from class: com.meferi.snscannerproduct_demo.ProductActivity$mHandler$1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i;
            Intrinsics.checkNotNullParameter(msg, "msg");
            super.handleMessage(msg);
            Log.d("PonService", "mHandler==" + msg.what);
            i = ProductActivity.this.MSG_DELEY_CLOSE;
            if (i == msg.what) {
                ProductActivity.this.finish();
            }
        }
    };
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(5894);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        getSupportActionBar().hide();
        hideSystemUI();

        initViews();
        showProduct();
        mHandler.sendEmptyMessageDelayed(this.MSG_DELEY_CLOSE, 10000L);
//        IntentFilter mFilter = new IntentFilter();
//        mFilter.addAction("com.meferi.action.CMD.QUICKSCAN");
//        registerReceiver(this.mReceiver, mFilter,Context.RECEIVER_EXPORTED);

    }

    /** findViewById once */
    private void initViews() {
        tvPrice       = findViewById(R.id.tv_price);
        tvProductName = findViewById(R.id.tv_product_name);
        tvWeight      = findViewById(R.id.tv_weight);
        tvBarCode     = findViewById(R.id.tv_bar_code);
        tvInfo        = findViewById(R.id.tv_info);
        image         = findViewById(R.id.image);
    }

    /** Read intent and populate UI */
    private void showProduct() {
        ProductBean product = getIntent().getParcelableExtra("product_key");
        if (product == null) return;

        tvPrice.setText(product.getPrice());
        tvProductName.setText(product.getName());
        tvWeight.setText(product.getPriceunt()); // 这里把单位放到 weight 框，可根据实际需求调整
        tvBarCode.setText(product.getBarcode());
        tvInfo.setText(product.getDbinfo());



        new Handler().post(() -> {
            Glide.with(ProductActivity.this)
                    .load(product.getImg())
                    .override(476, 476)
                    .placeholder(R.drawable.loading)
                    .centerCrop()
                    .into(image);
        });
    }
}