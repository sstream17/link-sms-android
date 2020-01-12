package xyz.stream.messenger.api.implementation.retrofit;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Response;

public class LoggingRetryableCallback<T> extends RetryableCallback<T> {

    private static final String TAG = "RetrofitResponse";

    private String logMessage;

    public LoggingRetryableCallback(Call<T> call, int totalRetries, String logMessage) {
        super(call, totalRetries);
        this.logMessage = logMessage;
    }

    @Override
    public void onFinalResponse(Call<T> call, Response<T> response) {
        try {
            Log.v(TAG, logMessage + ": SUCCESS");
        } catch (Exception e) { }
    }

    @Override
    public void onFinalFailure(Call<T> call, Throwable t) {
        try {
            Log.e(TAG, logMessage + ": FAILED");
        } catch (Exception e) { }
    }

}
