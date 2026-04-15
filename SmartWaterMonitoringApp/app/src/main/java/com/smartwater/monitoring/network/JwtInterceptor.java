package com.smartwater.monitoring.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtInterceptor implements Interceptor {

    public interface TokenProvider {
        String getToken();
    }

    private final TokenProvider tokenProvider;

    public JwtInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String token = tokenProvider != null ? tokenProvider.getToken() : null;
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request newReq = original.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(newReq);
    }
}
