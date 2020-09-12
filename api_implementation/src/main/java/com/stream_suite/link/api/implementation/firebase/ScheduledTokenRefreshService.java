package com.stream_suite.link.api.implementation.firebase;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.stream_suite.link.api.implementation.Account;

public class ScheduledTokenRefreshService extends IntentService {

    private static final int REQUEST_CODE = 3203;
    private static final long RUN_EVERY = 1000 * 60 * 120; // 2 hours

    public ScheduledTokenRefreshService() {
        super("ScheduledTokenRefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //TokenUtil.refreshToken(this);
        scheduleNextRun(this);
    }

    public static void scheduleNextRun(Context context) {
        if (!Account.INSTANCE.getPrimary()) {
            return;
        }

//        Intent intent = new Intent(context, ScheduledTokenRefreshService.class);
//        PendingIntent pIntent = PendingIntent.getService(context, REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        long currentTime = new Date().getTime();
//
//        alarmManager.cancel(pIntent);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + RUN_EVERY, pIntent);
    }
}
