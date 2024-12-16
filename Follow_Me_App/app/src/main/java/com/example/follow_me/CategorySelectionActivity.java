package com.example.follow_me;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class CategorySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        // 버튼 초기화
        Button clothingButton = findViewById(R.id.clothing_button);
        Button swimwearButton = findViewById(R.id.swimwear_button);
        Button cosmeticButton = findViewById(R.id.cosmetic_button);
        Button innerwearButton = findViewById(R.id.innerwear_button);
        Button fashionAccessoryButton = findViewById(R.id.fashion_accessory_button);
        Button luxuryButton = findViewById(R.id.luxury_button);
        Button backButton = findViewById(R.id.back_button);

        // 애니메이션 로드
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        // 왼쪽에서 슬라이드 애니메이션 적용
        clothingButton.startAnimation(slideInLeft);
        swimwearButton.startAnimation(slideInLeft);
        cosmeticButton.startAnimation(slideInLeft);

        // 오른쪽에서 슬라이드 애니메이션 적용
        innerwearButton.startAnimation(slideInRight);
        fashionAccessoryButton.startAnimation(slideInRight);
        luxuryButton.startAnimation(slideInRight);

        // 의류 버튼 클릭 이벤트
        clothingButton.setOnClickListener(v -> {
            String gender = getIntent().getStringExtra("gender");
            String age = getIntent().getStringExtra("age");
            String category = "의류";

            Intent intent = new Intent(CategorySelectionActivity.this, StyleSelectionActivity.class);
            intent.putExtra("gender", gender);
            intent.putExtra("age", age);
            intent.putExtra("category", category);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // BACK 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategorySelectionActivity.this, InfoSelectionActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}