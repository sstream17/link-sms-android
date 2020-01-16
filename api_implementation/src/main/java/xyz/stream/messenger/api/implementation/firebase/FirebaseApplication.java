package xyz.stream.messenger.api.implementation.firebase;

import android.app.Application;

public abstract class FirebaseApplication extends Application {
    public abstract FirebaseMessageHandler getFirebaseMessageHandler();
}
