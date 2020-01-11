/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.api;

import org.junit.Test;

import java.io.IOException;

import xyz.klinker.messenger.api.entity.AddMessagesRequest;
import xyz.klinker.messenger.api.entity.MessageBody;
import xyz.klinker.messenger.api.entity.UpdateMessageRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MessageTest extends ApiTest {

    @Test
    public void addAndUpdateMessages() throws IOException {
        String accountId = getAccountId();
        int originalSize = api.message().list(accountId, null, null, null).execute().body().length;

        MessageBody message = new MessageBody(1, 1, 1, "test", System.currentTimeMillis(),
                "text/plain", true, true, null, null, null, null);
        AddMessagesRequest request = new AddMessagesRequest(accountId, message);
        Object response = api.message().add(request).execute().body();
        assertNotNull(response);

        UpdateMessageRequest update = new UpdateMessageRequest(2, null, null, null);
        api.message().update(1, accountId, update).execute();

        MessageBody[] messages = api.message().list(accountId, null, null, null).execute().body();
        assertEquals(1, messages.length - originalSize);

        api.message().remove(1, accountId).execute();

        messages = api.message().list(accountId, null, null, null).execute().body();
        assertEquals(messages.length, originalSize);
    }

}
