package com.smartwater.monitoring.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private static final String PREFS = "SmartWaterPrefs";
    private static final String KEY_JWT = "jwt_token";

    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_JWT, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_JWT, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clear() {
        sp.edit().remove(KEY_JWT).apply();
    }
}
