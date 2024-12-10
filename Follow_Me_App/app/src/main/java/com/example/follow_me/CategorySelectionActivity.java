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
    }
}
