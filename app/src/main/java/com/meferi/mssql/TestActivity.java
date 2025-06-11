package com.meferi.mssql;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Random;


public class TestActivity extends AppCompatActivity {

    private TextView textResult;
    private EditText editSql;
    private final Random random = new Random();
    private static final String PREFS_NAME = "db_settings";

    private final int[] rgbColors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE
    };
    private int colorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        editSql = findViewById(R.id.editSql);
        textResult = findViewById(R.id.textResult);
        Button btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(v -> connectToDatabase());
    }

    private void connectToDatabase() {
        String sql = editSql.getText().toString().trim();

        if (sql.isEmpty()) {
            runOnUiThread(() -> textResult.setText("Please enter an SQL query."));
            return;
        }

        // 从 SharedPreferences 获取配置
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ip = prefs.getString(Constants.PREFS_IP, Constants.DEFAULT_API_ADDRESS);
        String user = prefs.getString(Constants.PREFS_USER, Constants.DEFAULT_USERNAME);
        String password = prefs.getString(Constants.PREFS_PASSWORD, Constants.DEFAULT_PASSWORD);
        String database = prefs.getString(Constants.PREFS_DATABASE, Constants.DEFAULT_DATABASE_NAME);
        String port = prefs.getString(Constants.PREFS_PORT, Constants.DEFAULT_PORT);
        new Thread(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database +
                        ";user=" + user + ";password=" + password + ";";

                Log.d("ddd", "url="+url);
                Connection connection = DriverManager.getConnection(url);
                Statement stmt = connection.createStatement();
                boolean hasResultSet = stmt.execute(sql);

                StringBuilder resultBuilder = new StringBuilder();

                if (hasResultSet) {
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            String columnValue = rs.getString(i);
                            resultBuilder.append(columnName).append("=").append(columnValue);
                            if (i < columnCount) resultBuilder.append(", ");
                        }
                        resultBuilder.append("\n");
                    }
                    rs.close();
                } else {
                    int updateCount = stmt.getUpdateCount();
                    resultBuilder.append("Update Count: ").append(updateCount);
                }

                stmt.close();
                connection.close();

                runOnUiThread(() -> {
                    textResult.setText(resultBuilder.toString());
                    textResult.setTextColor(rgbColors[colorIndex]);
                    colorIndex = (colorIndex + 1) % rgbColors.length;
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    textResult.setText("Error:\n" + e.getMessage());
                    textResult.setTextColor(Color.RED);
                });
            }
        }).start();
    }

    // 添加设置按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
