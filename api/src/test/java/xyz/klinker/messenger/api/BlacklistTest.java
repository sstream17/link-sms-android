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

import xyz.klinker.messenger.api.entity.AddBlacklistRequest;
import xyz.klinker.messenger.api.entity.BlacklistBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BlacklistTest extends ApiTest {

    @Test
    public void addAndRemove() throws IOException {
        String accountId = getAccountId();

        int originalSize = api.blacklist().list(accountId).execute().body().length;

        BlacklistBody blacklist = new BlacklistBody(1, "5154224558");
        AddBlacklistRequest request = new AddBlacklistRequest(accountId, blacklist);
        Object response = api.blacklist().add(request).execute().body();
        assertNotNull(response);

        int newSize = api.blacklist().list(accountId).execute().body().length;
        assertEquals(1, newSize - originalSize);

        api.blacklist().remove(1, accountId).execute();

        newSize = api.blacklist().list(accountId).execute().body().length;
        assertEquals(newSize, originalSize);
    }

}
