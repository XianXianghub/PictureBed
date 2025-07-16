package com.meferi.mssql.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.meferi.mssql.MyApp;
import com.meferi.mssql.db.ConfigEntity;
import com.meferi.mssql.db.ConfigManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRCodeUtil {
    /**
     * Generate barcode bitmap
     *
     * @param content  Content to encode
     * @param width    Image width
     * @param height   Image height
     * @param format   BarcodeFormat type (e.g. CODE_128, CODE_39, EAN_13)
     * @return Bitmap  Barcode bitmap
     */
    public static Bitmap createBarcodeBitmap(String content, int width, int height, BarcodeFormat format) {
        try {
            BitMatrix result = new MultiFormatWriter().encode(
                    content,
                    format,
                    width,
                    height,
                    null
            );

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
    /**
     * 生成数据库配置二维码Bitmap
     * @param context 上下文，用于初始化ConfigManager
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return 生成的二维码Bitmap，失败返回null
     */
    public static Bitmap generateConfigQRCode(Context context, int width, int height) {
        ConfigManager configManager = new ConfigManager(context);

        // 同步读取所有配置，注意不要在主线程调用
        List<ConfigEntity> configs = configManager.getAllConfigs();

        if (configs == null || configs.isEmpty()) return null;




        // 按id排序（假设数据库已经按id顺序，否则这里再排序）
        configs.sort((a, b) -> Integer.compare(a.id, b.id));
        StringBuilder sb = new StringBuilder();



        sb.append("mssql\u001c");
        String  version = MyApp.configVersion;
        sb.append(version);
        sb.append("\u001c");

        for(ConfigEntity config : configs){
            sb.append(config.value);
            sb.append("\u001c");
        }
        // 去除最后一个分号
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        Log.d("QRCodeUtil", "generateConfigQRCode: " + sb.toString()    );
        Log.d("QRCodeUtil", "sb.toString().lengt111: " + sb.toString().length()   );

        String qrContent = Utils.zipString(sb.toString());
        qrContent = Utils.CONFIG_PREFIX+qrContent+Utils.CONFIG_SUBFIX;

        Log.d("QRCodeUtil", "sb.toString().lengt222: " + qrContent.length()   );

        // 生成二维码Bitmap
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height, hints);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF; // 黑色或白色
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
