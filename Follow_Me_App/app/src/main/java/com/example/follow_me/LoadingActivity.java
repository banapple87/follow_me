package com.example.follow_me;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 이전 화면에서 전달된 데이터를 가져옵니다
        Intent intent = getIntent();
        String gender = intent.getStringExtra("gender");
        String age = intent.getStringExtra("age");
        String category = intent.getStringExtra("category");
        ArrayList<String> styles = intent.getStringArrayListExtra("styles");

        // UserSession에서 userId 가져오기
        String userId = UserSession.getInstance().getUserId();

        // 서버로 보낼 JSON 데이터 생성
        JSONObject data = new JSONObject();
        try {
            data.put("gender", gender);
            data.put("age", age);
            data.put("category", category);
            data.put("styles", styles);
            data.put("user_id", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 비동기 데이터 로딩 시작
        new FetchBrandAndRecommendationTask().execute(data.toString());
    }

    private class FetchBrandAndRecommendationTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject combinedResult = new JSONObject();
            try {
                // 브랜드 리스트 요청
                String brandListResponse = sendPostRequest("http://10.104.24.229:5003/submitData", params[0]);
                combinedResult.put("brandList", new JSONObject(brandListResponse));

                // 추천 리스트 요청
                String recommendationResponse = sendPostRequest("http://10.104.24.229:5004/recommend", params[0]);
                combinedResult.put("recommendationList", new JSONObject(recommendationResponse));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return combinedResult;
        }

        @Override
        protected void onPostExecute(JSONObject combinedResult) {
            if (combinedResult == null || combinedResult.length() == 0) {
                Toast.makeText(LoadingActivity.this, "서버 요청에 실패했습니다. 네트워크 상태를 확인하세요.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(LoadingActivity.this, BrandListActivity.class);
            intent.putExtra("combinedData", combinedResult.toString());
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }

        private String sendPostRequest(String urlString, String jsonInputString) {
            String response = "";
            try {
                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // JSON 데이터 전송
                java.io.OutputStream os = connection.getOutputStream();
                os.write(jsonInputString.getBytes("UTF-8"));
                os.flush();
                os.close();

                // 서버 응답 읽기
                int responseCode = connection.getResponseCode();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseContent.append(inputLine);
                }
                in.close();

                response = responseContent.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}