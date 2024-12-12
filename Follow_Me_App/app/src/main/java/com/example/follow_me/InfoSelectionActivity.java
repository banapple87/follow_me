package com.example.follow_me;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InfoSelectionActivity extends AppCompatActivity {

    private ImageView maleIcon, femaleIcon;
    private NumberPicker agePicker;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_selection);

        // 메인 컨테이너에 fade-in 애니메이션 적용
        LinearLayout mainContainer = findViewById(R.id.main_container);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainContainer.startAnimation(fadeIn);

        maleIcon = findViewById(R.id.male_icon);
        femaleIcon = findViewById(R.id.female_icon);
        agePicker = findViewById(R.id.age_picker);
        Button confirmButton = findViewById(R.id.confirm_button);
        Button backButton = findViewById(R.id.back_button);

        // 나이 선택 범위 설정
        agePicker.setMinValue(1);
        agePicker.setMaxValue(100);
        agePicker.setValue(25); // 기본 나이 25 설정

        // 성별 선택 이벤트 설정
        maleIcon.setOnClickListener(v -> selectGender(maleIcon, femaleIcon, "Male"));
        femaleIcon.setOnClickListener(v -> selectGender(femaleIcon, maleIcon, "Female"));

        // 확인 버튼 클릭 이벤트
        confirmButton.setOnClickListener(v -> {
            int selectedAge = agePicker.getValue();
            if (selectedGender.isEmpty()) {
                // 성별을 선택하지 않은 경우 Toast 메시지 표시
                Toast.makeText(this, "성별을 선택하세요", Toast.LENGTH_SHORT).show();
            } else {
                // 성별과 나이 정보를 Intent에 담아 다음 액티비티로 전달
                Intent intent = new Intent(InfoSelectionActivity.this, CategorySelectionActivity.class);
                intent.putExtra("gender", selectedGender);
                intent.putExtra("age", String.valueOf(selectedAge));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // BACK 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoSelectionActivity.this, SelectionActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });
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
