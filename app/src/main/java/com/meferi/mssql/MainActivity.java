package com.meferi.mssql;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText editIp, editUser, editPassword;
    private TextView textResult;
    private Random random = new Random();
    private EditText editSql;  // Add this field
    private EditText editDatabase; // add this field
    private EditText editPort; // Add this field

    private int colorIndex = 0;
    private final int[] rgbColors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE
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
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_home);
        getSupportActionBar().hide();
        hideSystemUI();

//        editIp = findViewById(R.id.editIp);
//        editUser = findViewById(R.id.editUser);
//        editPassword = findViewById(R.id.editPassword);
//        textResult = findViewById(R.id.textResult);
////        editSql = findViewById(R.id.editSql);
//        editDatabase = findViewById(R.id.editDatabase);
//        editPort = findViewById(R.id.editPort);

//        Button btnConnect = findViewById(R.id.btnConnect);

//        btnConnect.setOnClickListener(v -> connectToDatabase());
    }

    private void connectToDatabase() {
        String ip = editIp.getText().toString().trim();
        String user = editUser.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String sql = editSql.getText().toString().trim();
        String database = editDatabase.getText().toString().trim();
        String port = editPort.getText().toString().trim();

        if (sql.isEmpty()) {
            runOnUiThread(() -> textResult.setText("Please enter an SQL query."));
            return;
        }

        new Thread(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database + ";user=" + user + ";password=" + password + ";";


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
                            if (i < columnCount) {
                                resultBuilder.append(", ");
                            }
                        }
                        resultBuilder.append("\n");
                    }
                    rs.close();
                } else {
                    // For update/insert/delete, get update count
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
}
