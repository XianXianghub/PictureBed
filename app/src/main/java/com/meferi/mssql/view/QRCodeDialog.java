package com.meferi.mssql.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.meferi.mssql.R;

import java.io.File;
import java.io.FileOutputStream;

public class QRCodeDialog {


    public static void showQRCodePopup(Context context, View anchorView, Bitmap qrBitmap) {
        if (qrBitmap == null) {
            Toast.makeText(context, "二维码生成失败", Toast.LENGTH_SHORT).show();
            return;
        }

        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_qr_code, null);
        ImageView ivQRCode = popupView.findViewById(R.id.popupImageView);
        Button btnSave = popupView.findViewById(R.id.save_qr);

        ivQRCode.setImageBitmap(qrBitmap);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(10f);

        btnSave.setOnClickListener(v -> {
            String fileName = "sku_qrcode.png";
            Log.d("QRCodeDialog", "saveQRCodePopup: " + fileName);
            File savedFile = saveBitmapToPicturesFile(context, qrBitmap, fileName);
            if (savedFile != null) {
                Toast.makeText(context, "二维码已保存到:\n" + savedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            }
            popupWindow.dismiss();
        });

        // 居中弹出
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    /**
     * 保存Bitmap到公共相册Pictures目录
     * @param context 上下文
     * @param bitmap 图片
     * @param fileName 文件名，建议带后缀.png
     * @return 是否保存成功
     */
    public static File saveBitmapToPicturesFile(Context context, Bitmap bitmap, String fileName) {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "sku_config");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // 通知媒体库扫描（可选）
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
