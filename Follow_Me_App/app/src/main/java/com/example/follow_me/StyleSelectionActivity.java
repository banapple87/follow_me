package com.example.follow_me;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

        Button backButton = findViewById(R.id.back_button);

        // 메인 컨테이너에 fade-in 애니메이션 적용
        LinearLayout mainContainer = findViewById(R.id.main_container);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainContainer.startAnimation(fadeIn);

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

        // 슬라이딩 애니메이션 로드
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        for (Button button : buttons) {
            if ("left".equals(button.getTag())) {
                button.startAnimation(slideInLeft);
            } else if ("right".equals(button.getTag())) {
                button.startAnimation(slideInRight);
            }

            // 선택 핸들러 적용
            button.setOnClickListener(v -> handleSelection(button));
        }

        // 선택 완료 버튼
        Button completeButton = findViewById(R.id.complete_button);
        completeButton.setOnClickListener(v -> {
            if (selectedButtons.isEmpty()) {
                Toast.makeText(this, "스타일을 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
            } else {
                sendDataToServer();
            }
        });

        // BACK 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StyleSelectionActivity.this, CategorySelectionActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void handleSelection(Button button) {
        if (selectedButtons.contains(button)) {
            // 선택 해제: 흰색 배경으로 변경
            selectedButtons.remove(button);
            button.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
        } else {
            if (selectedButtons.size() < MAX_SELECTION) {
                // 선택: 검은색 배경으로 변경
                selectedButtons.add(button);
                button.setBackgroundTintList(getResources().getColorStateList(android.R.color.black));
            } else {
                Toast.makeText(this, "최대 2개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendDataToServer() {
        // 성별, 나이, 카테고리 선택 값
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
            String response = "";
            try {
                URL url = new URL("http://10.104.24.229:5003/submitData");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(params[0].getBytes("UTF-8"));
                os.flush();
                os.close();

                // 서버 응답 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseContent.append(inputLine);
                }
                in.close();
                response = responseContent.toString();
            } catch (Exception e) {
                e.printStackTrace();
                response = "Error: " + e.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // 서버로부터 받은 결과를 BrandListActivity로 전달
            Intent intent = new Intent(context, BrandListActivity.class);
            intent.putExtra("brandListJson", result);
            context.startActivity(intent);

            if (context instanceof StyleSelectionActivity) {
                ((StyleSelectionActivity) context).finish();
            }
        }
    }
}
