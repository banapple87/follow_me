package com.example.follow_me;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scroll);
        LinearLayout iconLayout = findViewById(R.id.icon_layout);

        horizontalScrollView.post(() -> {
            int centerX = (iconLayout.getWidth() - horizontalScrollView.getWidth()) / 2;
            smoothScrollToCenter(horizontalScrollView, centerX);
        });
    }

    private void smoothScrollToCenter(HorizontalScrollView scrollView, int targetX) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetX);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> {
            int scrollTo = (int) animation.getAnimatedValue();
            scrollView.scrollTo(scrollTo, 0);
        });
        animator.start();
    }
}
