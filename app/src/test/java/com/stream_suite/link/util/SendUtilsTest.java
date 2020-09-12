package com.stream_suite.link.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;

import com.stream_suite.link.MessengerRobolectricSuite;
import com.stream_suite.link.shared.util.SendUtils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SendUtilsTest extends MessengerRobolectricSuite {

    private SendUtils utils;
    private Context context = spy(RuntimeEnvironment.application);

    @Mock
    private TelephonyManager telephonyManager;

    @Before
    public void setUp() {
        utils = new SendUtils();
        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
    }

    @Test
    public void shouldSplit() {
        when(telephonyManager.getNetworkOperatorName()).thenReturn("U.S. Cellular");
        assertTrue(utils.shouldSplitMessages(context));
    }

    @Test
    public void shouldNotSplit() {
        when(telephonyManager.getNetworkOperatorName()).thenReturn("project_fi");
        assertFalse(utils.shouldSplitMessages(context));
    }

    @Test
    public void handlesNullOperator() {
        when(telephonyManager.getNetworkOperatorName()).thenReturn(null);
        assertFalse(utils.shouldSplitMessages(context));
    }

    @Test
    public void handlesBlankOperator() {
        when(telephonyManager.getNetworkOperatorName()).thenReturn("");
        assertFalse(utils.shouldSplitMessages(context));
    }
}
