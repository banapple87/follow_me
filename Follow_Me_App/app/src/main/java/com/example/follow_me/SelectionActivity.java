package com.example.follow_me;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    private LinearLayout shoppingIcon;
    private LinearLayout iconContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        String username = UserSession.getInstance().getUsername();

        TextView welcomeTextView = findViewById(R.id.subtitle_text);
        if (username != null) {
            welcomeTextView.setText(username + "님 안녕하세요.");
        }

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scroll);
        LinearLayout iconLayout = findViewById(R.id.icon_layout);
        shoppingIcon = findViewById(R.id.shopping_icon);
        iconContainer = findViewById(R.id.icon_container);

        horizontalScrollView.post(() -> {
            int centerX = (iconLayout.getWidth() - horizontalScrollView.getWidth()) / 2;
            smoothScrollToCenter(horizontalScrollView, centerX);
        });

        // 쇼핑 아이콘 클릭 시 InfoSelectionActivity로 이동
        shoppingIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, InfoSelectionActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void smoothScrollToCenter(HorizontalScrollView scrollView, int targetX) {
        int startX = scrollView.getScrollX();
        ValueAnimator animator = ValueAnimator.ofInt(startX, targetX);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int scrollTo = (int) animation.getAnimatedValue();
            scrollView.scrollTo(scrollTo, 0);
        });
        animator.start();
    }
}
