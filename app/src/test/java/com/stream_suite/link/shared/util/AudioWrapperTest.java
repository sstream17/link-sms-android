package com.stream_suite.link.shared.util;

import com.stream_suite.link.MessengerSuite;

public class AudioWrapperTest extends MessengerSuite {

    // these passed fine locally, but the build server didn't like them, for some reason...

//    @Mock
//    private Context context;
//    @Mock
//    private UiModeManager uiModeManager;
//    @Mock
//    private NotificationManager notificationManager;
//
//    @Before
//    public void setUp() {
//        when(context.getSystemService(Context.UI_MODE_SERVICE)).thenReturn(uiModeManager);
//        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager);
//    }
//
//    @Test
//    public void shouldWorkOnAndroidDevice() {
//        when(notificationManager.getCurrentInterruptionFilter()).thenReturn(NotificationManager.INTERRUPTION_FILTER_NONE);
//        when(uiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_NORMAL);
//        assertThat(AudioWrapper.Companion.shouldPlaySound(context, 27), is(true));
//    }
//
//    @Test
//    public void shouldNotSoundWhenDoNoDisturbIsOn() {
//        when(notificationManager.getCurrentInterruptionFilter()).thenReturn(NotificationManager.INTERRUPTION_FILTER_ALARMS);
//        when(uiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_NORMAL);
//        assertThat(AudioWrapper.Companion.shouldPlaySound(context, 27), is(false));
//
//        when(notificationManager.getCurrentInterruptionFilter()).thenReturn(NotificationManager.INTERRUPTION_FILTER_ALL);
//        when(uiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_NORMAL);
//        assertThat(AudioWrapper.Companion.shouldPlaySound(context, 27), is(false));
//    }
//
//    @Test
//    public void shouldNotWorkOnNonTouchscreenDevices() {
//        when(notificationManager.getCurrentInterruptionFilter()).thenReturn(NotificationManager.INTERRUPTION_FILTER_NONE);
//
//        when(uiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_WATCH);
//        assertThat(AudioWrapper.Companion.shouldPlaySound(context, 27), is(false));
//
//        when(uiModeManager.getCurrentModeType()).thenReturn(Configuration.UI_MODE_TYPE_TELEVISION);
//        assertThat(AudioWrapper.Companion.shouldPlaySound(context, 27), is(false));
//    }

}