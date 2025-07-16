package com.meferi.mssql.task;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "LoginTask";

    private final OnLoginListener mListener;

    public interface OnLoginListener {
        void onLoginSuccess(String token);
        void onLoginFailed();
    }

    public LoginTask(OnLoginListener listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length < 3) return null;

        String apiUrl = params[0];
        String username = params[1];
        String password = params[2];

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // JSON 请求体
            String jsonRequest = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            // 写入请求体
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            connection.disconnect();
            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "Login request failed", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject json = new JSONObject(result);
                int code = json.optInt("code", -1);

               if (code == 200) {
                    if (mListener != null) {
                        mListener.onLoginSuccess("");  // 调用成功回调
                    }
                } else {
                    Log.w(TAG, "Login failed, server response: " + result);
                    if (mListener != null) {
                        mListener.onLoginFailed();  // 调用失败回调
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "JSON parsing error", e);
                if (mListener != null) mListener.onLoginFailed();
            }
        } else {
            Log.e(TAG, "Login response is null");
            if (mListener != null) mListener.onLoginFailed();
        }

    }
}
