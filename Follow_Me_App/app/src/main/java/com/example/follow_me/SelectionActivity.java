package com.example.follow_me;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    private LinearLayout shoppingIcon;
    private ImageView maleIcon, femaleIcon;
    private NumberPicker agePicker;
    private String selectedGender = ""; // 선택된 성별을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scroll);
        LinearLayout iconLayout = findViewById(R.id.icon_layout);
        shoppingIcon = findViewById(R.id.shopping_icon);

        horizontalScrollView.post(() -> {
            int centerX = (iconLayout.getWidth() - horizontalScrollView.getWidth()) / 2;
            smoothScrollToCenter(horizontalScrollView, centerX);
        });

        shoppingIcon.setOnClickListener(v -> animateAndShowInputPopup());

        if (getIntent().getBooleanExtra("showPopup", false)) {
            new android.os.Handler().postDelayed(() -> showInputPopup(), 500);
        }
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

    private void animateAndShowInputPopup() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.7f,
                1.0f, 0.7f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);

        shoppingIcon.startAnimation(scaleAnimation);
        scaleAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) { }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                showInputPopup();
                resetAnimation();
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) { }
        });
    }

    private void resetAnimation() {
        ScaleAnimation resetAnimation = new ScaleAnimation(
                0.7f, 1.0f,
                0.7f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        resetAnimation.setDuration(200);
        shoppingIcon.startAnimation(resetAnimation);
    }

    private void showInputPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_input, null);
        builder.setView(popupView);

        maleIcon = popupView.findViewById(R.id.male_icon);
        femaleIcon = popupView.findViewById(R.id.female_icon);
        agePicker = popupView.findViewById(R.id.age_picker);
        Button okButton = popupView.findViewById(R.id.ok_button);
        Button cancelButton = popupView.findViewById(R.id.cancel_button);

        // 나이 선택 범위 설정
        agePicker.setMinValue(1);
        agePicker.setMaxValue(100);
        agePicker.setValue(25); // 기본 나이 25 설정

        // 성별 선택 이벤트 설정
        maleIcon.setOnClickListener(v -> selectGender(maleIcon, femaleIcon, "Male"));
        femaleIcon.setOnClickListener(v -> selectGender(femaleIcon, maleIcon, "Female"));

        AlertDialog dialog = builder.create();

        // 팝업창의 배경을 반투명하게 설정
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        okButton.setOnClickListener(view -> {
            int selectedAge = agePicker.getValue();
            if (selectedGender.isEmpty()) {
                // 성별을 선택하지 않은 경우 Toast 메시지를 표시하고 다음 페이지로 이동하지 않음
                Toast.makeText(this, "성별을 선택하세요", Toast.LENGTH_SHORT).show();
            } else {
                // 성별이 선택된 경우에만 다음 페이지로 이동
                Toast.makeText(this, "나이: " + selectedAge + ", 성별: " + selectedGender, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Intent intent = new Intent(SelectionActivity.this, CategorySelectionActivity.class);
                startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void selectGender(ImageView selectedIcon, ImageView otherIcon, String gender) {
        // 선택된 아이콘 확대
        selectedIcon.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
        // 다른 아이콘 원래 크기로 복원
        otherIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        // 선택된 성별 저장
        selectedGender = gender;
    }
}
