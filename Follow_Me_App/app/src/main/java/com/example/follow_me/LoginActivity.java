package com.example.follow_me;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String LOGIN_URL = "http://10.104.24.229:5002/api/login";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private LinearLayout loginElementsContainer;
    private LinearLayout textContainer;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(10, TimeUnit.SECONDS)    // 읽기 타임아웃
            .build();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        loginElementsContainer = findViewById(R.id.login_elements_container);
        textContainer = findViewById(R.id.text_container);

        // 초기 상태 설정
        loginElementsContainer.setVisibility(View.INVISIBLE);
        textContainer.setVisibility(View.INVISIBLE);

        // 페이드 인 애니메이션
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        textContainer.startAnimation(fadeIn);

        // 텍스트 컨테이너에 페이드 아웃 애니메이션 적용 후 로그인 요소 보이기
        new Handler().postDelayed(() -> {
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            textContainer.startAnimation(fadeOut);

            // 페이드 아웃 애니메이션이 끝난 후 로그인 요소 보이기
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    textContainer.setVisibility(View.GONE);
                    loginElementsContainer.setVisibility(View.VISIBLE);
                    loginElementsContainer.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

        }, 2000); // 2초 동안 텍스트 표시 후 애니메이션 실행

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener(v -> attemptLogin());

        // 회원가입 버튼 클릭 리스너
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("아이디를 입력하세요.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("비밀번호를 입력하세요.");
            return;
        }

        sendLoginRequest(username, password);
    }

    private void sendLoginRequest(String username, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
        } catch (Exception e) {
            Log.e(TAG, "JSON 오류", e);
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "서버 요청 실패", e);
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "서버 연결 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "응답: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject user = jsonResponse.getJSONObject("user");
                        String id = user.getString("id");
                        String name = user.getString("name");

                        // 싱글톤에 사용자 정보 저장
                        UserSession.getInstance().setUserId(id);
                        UserSession.getInstance().setUsername(name);

                        runOnUiThread(() -> {
                            Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(0, 0);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "JSON 파싱 오류", e);
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "응답 데이터 오류", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
