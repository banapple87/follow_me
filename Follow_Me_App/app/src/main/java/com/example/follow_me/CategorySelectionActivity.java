package com.example.follow_me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class CategorySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        Button clothingButton = findViewById(R.id.clothing_button);

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategorySelectionActivity.this, SelectionActivity.class);
            intent.putExtra("showPopup", true);
            startActivity(intent);
            finish();
        });

        // HOME 버튼 클릭 리스너
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategorySelectionActivity.this, SelectionActivity.class);
            startActivity(intent);
            finish();
        });

        clothingButton.setOnClickListener(v -> {
            // 이전 Activity에서 전달받은 성별과 나이 값
            String gender = getIntent().getStringExtra("gender");
            String age = getIntent().getStringExtra("age");
            String category = "의류";

            Intent intent = new Intent(CategorySelectionActivity.this, StyleSelectionActivity.class);
            intent.putExtra("gender", gender);
            intent.putExtra("age", age);
            intent.putExtra("category", category);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }
}