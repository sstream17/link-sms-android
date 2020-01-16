package xyz.stream.messenger.shared.util;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import xyz.stream.messenger.MessengerSuite;
import xyz.stream.messenger.shared.data.Settings;
import xyz.stream.messenger.shared.data.pojo.KeyboardLayout;

import static org.mockito.Mockito.*;

public class KeyboardLayoutHelperTest extends MessengerSuite {

    private static final int DEFAULT_INPUT_TYPE =
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT |
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE;

    private static final int DEFAULT_IME_OPTIONS = EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_EXTRACT_UI;

    @Mock
    private EditText editText;

    @Before
    public void setUp() {
        when(editText.getInputType()).thenReturn(DEFAULT_INPUT_TYPE);
        when(editText.getImeOptions()).thenReturn(DEFAULT_IME_OPTIONS);
    }

    @Test
    public void shouldAcceptDefaultKeyboard() {
        KeyboardLayoutHelper.INSTANCE.applyLayout(editText, KeyboardLayout.DEFAULT);

        verify(editText).setInputType(DEFAULT_INPUT_TYPE | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        verify(editText).setImeOptions(DEFAULT_IME_OPTIONS);
    }

    @Test
    public void shouldAcceptEnterKeyboard() {
        KeyboardLayoutHelper.INSTANCE.applyLayout(editText, KeyboardLayout.ENTER);

        verify(editText).setInputType(DEFAULT_INPUT_TYPE);
        verify(editText).setImeOptions(DEFAULT_IME_OPTIONS);
    }

    @Test
    public void shouldAcceptSendKeyboard() {
        KeyboardLayoutHelper.INSTANCE.applyLayout(editText, KeyboardLayout.SEND);

        verify(editText).setInputType(DEFAULT_INPUT_TYPE);
        verify(editText).setImeOptions(DEFAULT_IME_OPTIONS);
    }
}