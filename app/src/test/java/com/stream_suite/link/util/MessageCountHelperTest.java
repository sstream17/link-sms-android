package com.stream_suite.link.util;

import org.junit.Test;

import com.stream_suite.link.MessengerRobolectricSuite;
import com.stream_suite.link.shared.data.MmsSettings;
import com.stream_suite.link.shared.util.MessageCountHelper;

import static org.junit.Assert.*;

public class MessageCountHelperTest extends MessengerRobolectricSuite {

    // 4 characters
    private static final String ONE_MESSAGE = "test";

    // 132 characters
    private static final String ONE_LONGER_MESSAGE = "testing one two three four five six seven eight " +
            "nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen";

    // 265 characters
    private static final String TWO_MESSAGES = "testing one two three four five six seven eight nine " +
            "ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen testing " +
            "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen " +
            "sixteen seventeen eighteen nineteen";

    @Test
    public void neverConvertToMMS() {
        MmsSettings.INSTANCE.setConvertLongMessagesToMMS(false);

        assertEquals(null, MessageCountHelper.INSTANCE.getMessageCounterText(ONE_MESSAGE));
        assertEquals("1/28", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_LONGER_MESSAGE));
        assertEquals("2/41", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES));
    }

    @Test
    public void withConvertingToMMSAfterOneMessage() {
        MmsSettings.INSTANCE.setConvertLongMessagesToMMS(true);
        MmsSettings.INSTANCE.setNumberOfMessagesBeforeMms(1);

        assertEquals("", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_MESSAGE));
        assertEquals("1/28", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_LONGER_MESSAGE));
        assertEquals("MMS", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES));
        assertEquals("MMS", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES + TWO_MESSAGES));
    }

    @Test
    public void withConvertingToMMSAfterTwoMessages() {
        MmsSettings.INSTANCE.setConvertLongMessagesToMMS(true);
        MmsSettings.INSTANCE.setNumberOfMessagesBeforeMms(2);

        assertEquals("", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_MESSAGE));
        assertEquals("1/28", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_LONGER_MESSAGE));
        assertEquals("2/41", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES));
        assertEquals("MMS", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES + ONE_LONGER_MESSAGE));
    }

    @Test
    public void withConvertingToMMSAfterThreeMessages() {
        MmsSettings.INSTANCE.setConvertLongMessagesToMMS(true);
        MmsSettings.INSTANCE.setNumberOfMessagesBeforeMms(3);

        assertEquals("", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_MESSAGE));
        assertEquals("1/28", MessageCountHelper.INSTANCE.getMessageCounterText(ONE_LONGER_MESSAGE));
        assertEquals("2/41", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES));
        assertEquals("3/62", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES + ONE_LONGER_MESSAGE));
        assertEquals("MMS", MessageCountHelper.INSTANCE.getMessageCounterText(TWO_MESSAGES + TWO_MESSAGES));
    }
}