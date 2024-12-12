package com.example.follow_me;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    private LinearLayout shoppingIcon;
    private LinearLayout iconContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scroll);
        LinearLayout iconLayout = findViewById(R.id.icon_layout);
        shoppingIcon = findViewById(R.id.shopping_icon);
        iconContainer = findViewById(R.id.icon_container);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        iconContainer.startAnimation(fadeIn);

        horizontalScrollView.post(() -> {
            int centerX = (iconLayout.getWidth() - horizontalScrollView.getWidth()) / 2;
            smoothScrollToCenter(horizontalScrollView, centerX);
        });

        // 쇼핑 아이콘 클릭 시 InfoSelectionActivity로 이동
        shoppingIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, InfoSelectionActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
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
