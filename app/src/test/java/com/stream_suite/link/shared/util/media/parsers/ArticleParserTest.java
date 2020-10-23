package com.stream_suite.link.shared.util.media.parsers;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import com.stream_suite.link.MessengerRobolectricSuite;
import com.stream_suite.link.shared.data.MimeType;
import com.stream_suite.link.shared.data.model.Message;

import static org.junit.Assert.*;

public class ArticleParserTest extends MessengerRobolectricSuite {

    private ArticleParser parser;

    @Before
    public void setUp() {
        parser = new ArticleParser(null);
    }

    @Test
    public void mimeType() {
        assertThat(parser.getMimeType(), CoreMatchers.is(MimeType.INSTANCE.getMEDIA_ARTICLE()));
    }

    @Test
    public void shouldParseWebUrl() {
        assertThat(parser.canParse(makeMessage("klinkerapps.com")), CoreMatchers.is(true));

        setUp();
        assertThat(parser.canParse(makeMessage("https://www.klinkerapps.com")), CoreMatchers.is(true));

        setUp();
        assertThat(parser.canParse(makeMessage("http://klinkerapps.com")), CoreMatchers.is(true));

        setUp();
        assertThat(parser.canParse(makeMessage("https://example.com/testing?testing+again")), CoreMatchers.is(true));
    }

    @Test
    public void shouldNotParseNonWebText() {
        assertThat(parser.canParse(makeMessage("dont match")), CoreMatchers.is(false));

        setUp();
        assertThat(parser.canParse(makeMessage("test/co")), CoreMatchers.is(false));

        setUp();
        assertThat(parser.canParse(makeMessage("hey.fomda")), CoreMatchers.is(false));
    }

    private Message makeMessage(String text) {
        Message m = new Message();
        m.setData(text);
        m.setMimeType(MimeType.INSTANCE.getTEXT_PLAIN());

        return m;
    }
}