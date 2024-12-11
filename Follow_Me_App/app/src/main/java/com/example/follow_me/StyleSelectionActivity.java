package com.example.follow_me;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import org.json.JSONObject;

public class StyleSelectionActivity extends AppCompatActivity {

    private HashSet<Button> selectedButtons = new HashSet<>();
    private static final int MAX_SELECTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_selection);

        // 뒤로 가기 및 홈 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StyleSelectionActivity.this, CategorySelectionActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(StyleSelectionActivity.this, SelectionActivity.class);
            startActivity(intent);
            finish();
        });

        // 해시태그 버튼 초기화
        Button[] buttons = {
                findViewById(R.id.btn1),
                findViewById(R.id.btn2),
                findViewById(R.id.btn3),
                findViewById(R.id.btn4),
                findViewById(R.id.btn5),
                findViewById(R.id.btn6),
                findViewById(R.id.btn7),
                findViewById(R.id.btn8),
                findViewById(R.id.btn9),
                findViewById(R.id.btn10),
                findViewById(R.id.btn11),
                findViewById(R.id.btn12),
                findViewById(R.id.btn13),
        };

        for (Button button : buttons) {
            button.setOnClickListener(v -> handleSelection(button));
        }

        // 선택 완료 버튼
        Button completeButton = findViewById(R.id.complete_button);
        completeButton.setOnClickListener(v -> sendDataToServer());
    }

    private void handleSelection(Button button) {
        if (selectedButtons.contains(button)) {
            selectedButtons.remove(button);
            button.setBackgroundTintList(getColorStateList(android.R.color.white));
            button.setTextColor(getColorStateList(android.R.color.black));
        } else {
            if (selectedButtons.size() < MAX_SELECTION) {
                selectedButtons.add(button);
                button.setBackgroundTintList(getColorStateList(android.R.color.black));
                button.setTextColor(getColorStateList(android.R.color.white));
            } else {
                Toast.makeText(this, "최대 2개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendDataToServer() {
        // 성별, 나이, 카테고리 선택 값 (예제 값)
        String gender = getIntent().getStringExtra("gender");
        String age = getIntent().getStringExtra("age");
        String category = getIntent().getStringExtra("category");

        // 스타일 선택 값
        HashSet<String> styles = new HashSet<>();
        for (Button button : selectedButtons) {
            styles.add(button.getText().toString());
        }

        // 서버로 보낼 데이터
        JSONObject data = new JSONObject();
        try {
            data.put("gender", gender);
            data.put("age", age);
            data.put("category", category);
            data.put("styles", styles);

            // 비동기 데이터 전송 (Context 전달)
            new SendToServerTask(this).execute(data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SendToServerTask extends AsyncTask<String, Void, String> {
        private Context context;

        public SendToServerTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            String responseMessage = "";
            try {
                URL url = new URL("http://10.104.24.229:5000/submitData");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // 데이터 전송
                OutputStream os = connection.getOutputStream();
                os.write(params[0].getBytes("UTF-8"));
                os.flush();
                os.close();

                // 서버 응답 확인
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    responseMessage = "Data sent successfully!";
                } else {
                    responseMessage = "Failed to send data. Response Code: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                responseMessage = "Error: " + e.getMessage();
            }
            return responseMessage;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }
    }
}
