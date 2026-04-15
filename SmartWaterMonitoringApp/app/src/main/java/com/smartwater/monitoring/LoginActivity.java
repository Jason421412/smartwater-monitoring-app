package com.smartwater.monitoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.AuthApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.LoginReq;
import com.smartwater.monitoring.network.dto.LoginResp;
import com.smartwater.monitoring.network.dto.ResendVerificationRequest;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private TextView tvServerStatus;  // ✅ 新增：显示当前服务器地址

    private AuthApi authApi;
    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenStore = new TokenStore(this);

        initViews();
        setupClicks();
        updateServerStatus();  // ✅ 显示当前服务器状态

        // 如果已经有 token，直接进 Dashboard（optional）
        if (tokenStore.isLoggedIn()) {
            navigateToDashboard();
        }
    }

    private void initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvServerStatus = findViewById(R.id.tvServerStatus);  // ✅ 新增
    }

    private void setupClicks() {
        btnLogin.setOnClickListener(v -> loginUser());

        // ✅ 长按登录按钮：配置服务器地址
        btnLogin.setOnLongClickListener(v -> {
            showServerConfigDialog();
            return true;
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // ✅ 点击服务器状态也可以配置
        if (tvServerStatus != null) {
            tvServerStatus.setOnClickListener(v -> showServerConfigDialog());
        }
    }

    // ✅ 更新服务器状态显示
    private void updateServerStatus() {
        if (tvServerStatus != null) {
            String currentUrl = ApiClient.getBaseUrl(this);
            tvServerStatus.setText("Server: " + currentUrl + " (tap to change)");
        }
        // 重新创建 API 客户端使用最新 URL
        authApi = ApiClient.createAuth(this);
    }

    // ✅ 显示服务器配置对话框
    private void showServerConfigDialog() {
        String currentUrl = ApiClient.getBaseUrl(this);
        
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(currentUrl);
        input.setSelectAllOnFocus(true);
        input.setPadding(48, 32, 48, 32);
        
        new AlertDialog.Builder(this)
                .setTitle("⚙️ Server Configuration")
                .setMessage("Enter the backend server URL:\n\n" +
                        "• Emulator: http://10.0.2.2:8080/\n" +
                        "• Real device: http://YOUR_PC_IP:8080/\n\n" +
                        "Current: " + currentUrl)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUrl = input.getText().toString().trim();
                    if (!newUrl.isEmpty()) {
                        ApiClient.setBaseUrl(this, newUrl);
                        updateServerStatus();
                        Toast.makeText(this, "✅ Server URL updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Reset Default", (dialog, which) -> {
                    ApiClient.resetToDefaultUrl(this);
                    updateServerStatus();
                    Toast.makeText(this, "✅ Reset to default!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loginUser() {
        String email = safeText(etLoginEmail);
        String password = safeText(etLoginPassword);

        if (email.isEmpty()) {
            etLoginEmail.setError("Email is required");
            etLoginEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        authApi.login(new LoginReq(email, password)).enqueue(new Callback<LoginResp>() {
            @Override
            public void onResponse(Call<LoginResp> call, Response<LoginResp> resp) {
                btnLogin.setEnabled(true);

                if (resp.isSuccessful() && resp.body() != null) {
                    String token = resp.body().getAnyToken();
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(LoginActivity.this,
                                "Login OK but token missing. Check backend response fields.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    tokenStore.saveToken(token);
                    Toast.makeText(LoginActivity.this, "Login Successful ✅", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                    return;
                }

                // 登录失败：可能是 "未验证邮箱"
                String err = readError(resp.errorBody());
                if (err.toLowerCase().contains("verify") || err.toLowerCase().contains("verified")) {
                    showVerifyDialog(email);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login failed (HTTP " + resp.code() + "): " + (err.isEmpty() ? resp.message() : err),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResp> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, 
                        "⚠️ Network error: " + t.getMessage() + 
                        "\n\nLong-press LOGIN to configure server address", 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showVerifyDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email not verified")
                .setMessage("You must verify your email before login.\n\nResend verification email?")
                .setPositiveButton("Resend", (d, w) -> resendVerification(email))
                .setNegativeButton("OK", null)
                .show();
    }

    private void resendVerification(String email) {
        authApi.resendVerification(new ResendVerificationRequest(email))
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                        if (resp.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Verification email resent ✅", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Resend failed (HTTP " + resp.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Resend error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String readError(ResponseBody body) {
        if (body == null) return "";
        try {
            return body.string();
        } catch (IOException e) {
            return "";
        }
    }
}

