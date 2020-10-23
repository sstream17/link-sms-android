package com.stream_suite.link.api.implementation.retrofit;

public interface ApiErrorPersister {

    void onAddConversationError(long conversationId);
    void onAddMessageError(long messageId);

}
