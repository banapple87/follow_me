package com.example.follow_me;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;

public class StyleSelectionActivity extends AppCompatActivity {

    private HashSet<Button> selectedButtons = new HashSet<>();
    private static final int MAX_SELECTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_selection);

        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StyleSelectionActivity.this, CategorySelectionActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });

        // HOME 버튼 클릭 리스너
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(StyleSelectionActivity.this, SelectionActivity.class);
            startActivity(intent);
            finish();
        });

        // 버튼 초기화
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
    }

    private void handleSelection(Button button) {
        if (selectedButtons.contains(button)) {
            // 선택 해제 시: 배경을 검은색, 글자를 흰색으로 변경
            selectedButtons.remove(button);
            button.setBackgroundTintList(getColorStateList(android.R.color.white));
            button.setTextColor(getColorStateList(android.R.color.black));
        } else {
            if (selectedButtons.size() < MAX_SELECTION) {
                // 선택 시: 배경을 흰색, 글자를 검은색으로 변경
                selectedButtons.add(button);
                button.setBackgroundTintList(getColorStateList(android.R.color.black));
                button.setTextColor(getColorStateList(android.R.color.white));
            } else {
                // 최대 선택 수를 초과할 때 알림
                Toast.makeText(this, "최대 2개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}