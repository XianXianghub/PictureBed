package com.meferi.mssql.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meferi.mssql.view.QRCodeDialog;
import com.meferi.mssql.tool.QRCodeUtil;
import com.meferi.mssql.R;

import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private static class MenuItem {
        String title;
        Runnable action;

        MenuItem(String title, Runnable action) {
            this.title = title;
            this.action = action;
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
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();
        hideSystemUI();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<MenuItem> items = Arrays.asList(
                new MenuItem(getString(R.string.menu_server_config), () -> startActivity(new Intent(this, ServerSettingsActivity.class))),
                new MenuItem(getString(R.string.menu_product_param_config), () -> startActivity(new Intent(this, DbSettingsActivity.class))),
                new MenuItem(getString(R.string.menu_settings), () -> startActivity(new Intent(this, SettingsActivity.class))),
                new MenuItem(getString(R.string.menu_export_settings), () -> {
                    ProgressDialog progressDialog = new ProgressDialog(MenuActivity.this);
                    progressDialog.setMessage(getString(R.string.generating_qr_code));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new Thread(() -> {
                        Bitmap qrBitmap = QRCodeUtil.generateConfigQRCode(MenuActivity.this, 500, 500);
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            if (qrBitmap != null) {
                                QRCodeDialog.showQRCodePopup(MenuActivity.this, getWindow().getDecorView(), qrBitmap);
                            } else {
                                Toast.makeText(MenuActivity.this, getString(R.string.qr_code_generation_failed), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
        );

        recyclerView.setAdapter(new MenuAdapter(items));
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        private final List<MenuItem> menuItems;

        MenuAdapter(List<MenuItem> items) {
            this.menuItems = items;
        }

        @Override
        public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_menu_card, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MenuViewHolder holder, int position) {
            MenuItem item = menuItems.get(position);
            holder.title.setText(item.title);
            holder.card.setOnClickListener(v -> item.action.run());
        }

        @Override
        public int getItemCount() {
            return menuItems.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            CardView card;

            MenuViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.menu_title);
                card = itemView.findViewById(R.id.menu_card);
            }
        }
    }
}
