package com.stream_suite.link.api.implementation.firebase;

import android.app.Application;

public abstract class FirebaseApplication extends Application {
    public abstract FirebaseMessageHandler getFirebaseMessageHandler();
}
