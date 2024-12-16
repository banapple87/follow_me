package com.example.follow_me;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

        // 사용자 이름을 UserSession에서 가져와 타이틀에 반영
        String username = UserSession.getInstance().getUsername();
        TextView similarCustomersTitle = findViewById(R.id.similar_customers_title);
        TextView similarStoresTitle = findViewById(R.id.similar_stores_title);

        similarCustomersTitle.setText(username + "님과 취향이 비슷한 고객님들이 방문한 매장");
        similarStoresTitle.setText(username + "님이 방문한 매장과 비슷한 매장");

        // 메인 컨테이너에 fade-in 애니메이션 적용
        LinearLayout mainContainer = findViewById(R.id.main_container);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainContainer.startAnimation(fadeIn);

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

        // 추천 브랜드 RecyclerView 항목을 길게 눌러 브랜드 리스트에 추가하기
        setupLongClickForRecommendation(similarCustomersRecyclerView, similarCustomersList);
        setupLongClickForRecommendation(similarStoresRecyclerView, similarStoresList);

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

    // 추천 브랜드 항목을 길게 눌렀을 때 브랜드 리스트에 추가하는 메서드
    private void setupLongClickForRecommendation(RecyclerView recyclerView, ArrayList<String> recommendationList) {
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 항목 클릭 이벤트 (필요시 구현)
            }

            @Override
            public void onItemLongClick(int position) {
                String selectedBrand = recommendationList.get(position);
                if (!brandList.contains(selectedBrand)) {
                    brandList.add(0, selectedBrand);
                    brandAdapter.notifyDataSetChanged();
                    Toast.makeText(BrandListActivity.this, selectedBrand + "이(가) 브랜드 리스트에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BrandListActivity.this, selectedBrand + "은(는) 이미 브랜드 리스트에 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }));
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
