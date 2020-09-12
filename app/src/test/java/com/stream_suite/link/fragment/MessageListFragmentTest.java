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

package com.stream_suite.link.fragment;

import android.graphics.Color;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.stream_suite.link.MessengerRobolectricSuite;
import com.stream_suite.link.fragment.message.MessageInstanceManager;
import com.stream_suite.link.fragment.message.MessageListFragment;
import com.stream_suite.link.shared.data.model.Conversation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MessageListFragmentTest extends MessengerRobolectricSuite {

    private MessageListFragment fragment;

    @Before
    public void setUp() {
        Conversation conversation = new Conversation();
        conversation.setTitle("Luke Klinker");
        conversation.setPhoneNumbers("(515) 991-1493");
        conversation.getColors().setColor(Color.RED);
        conversation.getColors().setColorDark(Color.BLUE);
        conversation.getColors().setColorAccent(Color.YELLOW);

        fragment = startFragment(MessageInstanceManager.Companion.newInstance(conversation, -1, true));
    }

    @Test @Ignore
    public void notNull() {
        assertNotNull(fragment);
    }

    @Test @Ignore
    public void isAdded() {
        assertTrue(fragment.isAdded());
    }

}