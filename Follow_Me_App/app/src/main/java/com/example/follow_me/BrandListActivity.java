package com.example.follow_me;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BrandListActivity extends AppCompatActivity {

    private ListView brandListView;
    private ArrayAdapter<String> brandAdapter;
    private ArrayList<String> brandList = new ArrayList<>();

    private RecyclerView similarCustomersRecyclerView;
    private RecyclerView similarStoresRecyclerView;

    private HorizontalListAdapter similarCustomersAdapter;
    private HorizontalListAdapter similarStoresAdapter;

    private ArrayList<String> similarCustomersList = new ArrayList<>();
    private ArrayList<String> similarStoresList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_list);

        Button backButton = findViewById(R.id.back_button);
        brandListView = findViewById(R.id.brand_list_view);

        // 브랜드 리스트 어댑터 설정
        brandAdapter = new ArrayAdapter<>(this, R.layout.list_item_brand, brandList);
        brandListView.setAdapter(brandAdapter);

        // 추천 브랜드 RecyclerView 초기화
        similarCustomersRecyclerView = findViewById(R.id.similar_customers_recycler_view);
        similarStoresRecyclerView = findViewById(R.id.similar_stores_recycler_view);

        setupRecyclerView(similarCustomersRecyclerView, similarCustomersList);
        setupRecyclerView(similarStoresRecyclerView, similarStoresList);

        // 서버에서 받은 데이터 파싱 및 표시
        String combinedData = getIntent().getStringExtra("combinedData");
        if (combinedData != null) {
            parseAndDisplayData(combinedData);
        }

        // 리스트 아이템을 길게 눌러 삭제하기
        brandListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String brand = brandList.get(position);
            brandList.remove(position);
            brandAdapter.notifyDataSetChanged();
            Toast.makeText(this, brand + "이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            return true;
        });

        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrandListActivity.this, MainActivity.class);
            intent.putStringArrayListExtra("brandList", brandList);
            startActivity(intent);
        });

        // BACK 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrandListActivity.this, StyleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void parseAndDisplayRecommendations(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("collaborative_recommendations")) {
                JSONArray similarCustomersArray = jsonObject.getJSONArray("collaborative_recommendations");
                similarCustomersList.clear();
                for (int i = 0; i < similarCustomersArray.length(); i++) {
                    similarCustomersList.add(similarCustomersArray.getString(i));
                }
                similarCustomersAdapter.notifyDataSetChanged();
            }

            if (jsonObject.has("content_based_recommendations")) {
                JSONArray similarStoresArray = jsonObject.getJSONArray("content_based_recommendations");
                similarStoresList.clear();
                for (int i = 0; i < similarStoresArray.length(); i++) {
                    similarStoresList.add(similarStoresArray.getString(i));
                }
                similarStoresAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "추천 데이터를 불러오는 데 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView, ArrayList<String> dataList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        HorizontalListAdapter adapter = new HorizontalListAdapter(dataList);
        recyclerView.setAdapter(adapter);

        if (recyclerView == similarCustomersRecyclerView) {
            similarCustomersAdapter = adapter;
        } else {
            similarStoresAdapter = adapter;
        }
    }

    private void parseAndDisplayData(String json) {
        try {
            JSONObject combinedObject = new JSONObject(json);

            // 브랜드 리스트 파싱
            JSONObject brandListObject = combinedObject.getJSONObject("brandList");
            if (brandListObject.has("brands")) {
                JSONArray brandsArray = brandListObject.getJSONArray("brands");
                brandList.clear();
                for (int i = 0; i < brandsArray.length(); i++) {
                    brandList.add(brandsArray.getString(i));
                }
                brandAdapter.notifyDataSetChanged();
            }

            // 추천 리스트 파싱
            JSONObject recommendationObject = combinedObject.getJSONObject("recommendationList");
            if (recommendationObject.has("collaborative_recommendations")) {
                JSONArray similarCustomersArray = recommendationObject.getJSONArray("collaborative_recommendations");
                similarCustomersList.clear();
                for (int i = 0; i < similarCustomersArray.length(); i++) {
                    similarCustomersList.add(similarCustomersArray.getString(i));
                }
                similarCustomersAdapter.notifyDataSetChanged();
            }

            if (recommendationObject.has("content_based_recommendations")) {
                JSONArray similarStoresArray = recommendationObject.getJSONArray("content_based_recommendations");
                similarStoresList.clear();
                for (int i = 0; i < similarStoresArray.length(); i++) {
                    similarStoresList.add(similarStoresArray.getString(i));
                }
                similarStoresAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터를 불러오는 데 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
