package com.example.follow_me;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BrandListActivity extends AppCompatActivity {

    private ListView brandListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> brandList = new ArrayList<>();
    private String apiUrl = "http://10.104.24.229:5003/getBrands"; // 서버의 브랜드 리스트 API URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_list);

        brandListView = findViewById(R.id.brand_list_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, brandList);
        brandListView.setAdapter(adapter);

        // 전달받은 JSON 데이터 파싱
        String brandListJson = getIntent().getStringExtra("brandListJson");
        parseAndDisplayBrands(brandListJson);

        // 리스트 아이템을 길게 눌러 삭제하기
        brandListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String brand = brandList.get(position);
            brandList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, brand + "이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            return true;
        });

        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrandListActivity.this, MainActivity.class);
            intent.putStringArrayListExtra("brandList", brandList);
            startActivity(intent);
        });
    }

    private void parseAndDisplayBrands(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("brands");
            for (int i = 0; i < jsonArray.length(); i++) {
                brandList.add(jsonArray.getString(i));
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "브랜드 리스트를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    // 서버에서 브랜드 리스트를 가져오는 비동기 작업
    private class FetchBrandListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder responseContent = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responseContent.append(inputLine);
                    }
                    in.close();
                    response = responseContent.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "Error: " + e.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("brands");
                for (int i = 0; i < jsonArray.length(); i++) {
                    brandList.add(jsonArray.getString(i));
                }
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(BrandListActivity.this, "브랜드 리스트를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
