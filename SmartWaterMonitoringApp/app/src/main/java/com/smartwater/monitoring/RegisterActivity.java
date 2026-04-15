package com.smartwater.monitoring;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.AuthApi;
import com.smartwater.monitoring.network.dto.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ImageButton btnBackToLogin;
    private TextInputEditText etRegisterName, etRegisterEmail, etRegisterPhone;
    private TextInputEditText etRegisterPassword, etRegisterConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authApi = ApiClient.createAuth(this); // ✅ uses dynamic BASE_URL

        initViews();
        setupClicks();
    }

    private void initViews() {
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupClicks() {
        btnBackToLogin.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = safeText(etRegisterName);
        String email = safeText(etRegisterEmail);
        String phone = safeText(etRegisterPhone);
        String password = safeText(etRegisterPassword);
        String confirm = safeText(etRegisterConfirmPassword);

        if (fullName.isEmpty()) {
            etRegisterName.setError("Full name is required");
            etRegisterName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etRegisterEmail.setError("Email is required");
            etRegisterEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Please enter a valid email");
            etRegisterEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etRegisterPhone.setError("Phone number is required");
            etRegisterPhone.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etRegisterPassword.setError("Password is required");
            etRegisterPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            etRegisterPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etRegisterConfirmPassword.setError("Passwords do not match");
            etRegisterConfirmPassword.requestFocus();
            return;
        }

        // ✅ 方案A：拆 firstName / lastName（避免你后端 first_name 不能为 null）
        String[] parts = fullName.trim().split("\\s+");
        String firstName = parts[0];
        String lastName = (parts.length >= 2) ? joinFrom(parts, 1) : "NA"; // lastName 必须非空

        RegisterRequest body = new RegisterRequest(firstName, lastName, email, password, phone);

        btnRegister.setEnabled(false);

        authApi.register(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                btnRegister.setEnabled(true);

                if (resp.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Registered ✅ Please verify your email then login.",
                            Toast.LENGTH_LONG).show();

                    // 打开邮箱 App（optional but useful）
                    openEmailApp();

                    finish(); // back to login
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Register failed (HTTP " + resp.code() + "): " + resp.message(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this,
                        "Register failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openEmailApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            startActivity(intent);
        } catch (Exception e) {
            // fallback: open gmail web (optional)
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/"));
            startActivity(browser);
        }
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String joinFrom(String[] arr, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
