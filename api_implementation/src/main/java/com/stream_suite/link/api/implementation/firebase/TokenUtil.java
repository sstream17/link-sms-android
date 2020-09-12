package com.stream_suite.link.api.implementation.firebase;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import com.stream_suite.link.api.implementation.Account;
import com.stream_suite.link.api.implementation.ApiUtils;

public class TokenUtil {
    private static final String TAG = "FCMTokenRefresh";

    public static void refreshToken(Context context) {
        Log.v(TAG, "starting refresh");

        final Account account = Account.INSTANCE;
        final String token = FirebaseInstanceId.getInstance().getToken();

        Log.v(TAG, "token: " + token);
        if (account.exists() && token != null) {
            Log.v(TAG, "refreshing on server");
            new Thread(() -> {
                ApiUtils api = ApiUtils.INSTANCE;
                api.updateDevice(account.getAccountId(), Integer.parseInt(account.getDeviceId()), Build.MODEL,
                        FirebaseInstanceId.getInstance().getToken());
            }).start();
        }

        FirebaseMessaging.getInstance().subscribeToTopic("feature_flag");
    }

    public static void updateToken(String token) {
        final Account account = Account.INSTANCE;

        Log.v(TAG, "token: " + token);
        if (account.exists() && token != null) {
            Log.v(TAG, "refreshing on server");
            new Thread(() -> {
                ApiUtils api = ApiUtils.INSTANCE;
                api.updateDevice(account.getAccountId(), Integer.parseInt(account.getDeviceId()), Build.MODEL,
                        FirebaseInstanceId.getInstance().getToken());
            }).start();
        }

        FirebaseMessaging.getInstance().subscribeToTopic("feature_flag");
    }
}
