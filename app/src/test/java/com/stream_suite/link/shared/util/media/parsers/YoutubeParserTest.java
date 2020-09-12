package com.stream_suite.link.shared.util.media.parsers;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.stream_suite.link.MessengerRobolectricSuite;
import com.stream_suite.link.shared.data.MimeType;
import com.stream_suite.link.shared.data.model.Message;

import static org.junit.Assert.*;

public class YoutubeParserTest extends MessengerRobolectricSuite {

    private YoutubeParser parser;

    @Before
    public void setUp() {
        parser = new YoutubeParser(null);
    }

    @Test
    public void mimeType() {
        assertThat(parser.getMimeType(), Matchers.is(MimeType.INSTANCE.getMEDIA_YOUTUBE_V2()));
    }

    @Test
    public void shouldParseVideoUrl() {
        assertThat(parser.canParse(makeMessage("youtu.be/")), Matchers.is(true));
        assertThat(parser.canParse(makeMessage("youtube.com/")), Matchers.is(true));
        assertThat(parser.canParse(makeMessage("https://youtu.be/ohGXwX6zKow")), Matchers.is(true));
        assertThat(parser.canParse(makeMessage("https://www.youtube.com/watch?v=ohGXwX6zKow")), Matchers.is(true));
    }

    @Test
    public void shouldNotParseNonVideoUrl() {
        assertThat(parser.canParse(makeMessage("https://www.youtube.com/channel/UCycZgQlj27nwMyGSihghSig/videos")), Matchers.is(false));
        assertThat(parser.canParse(makeMessage("https://www.youtube.com/channel/UCycZgQlj27nwMyGSihghSig/playlist")), Matchers.is(false));
        assertThat(parser.canParse(makeMessage("https://www.youtube.com/user/klinker24")), Matchers.is(false));
    }

    @Test
    public void shouldConvertThumbnailBackToVideoUri() {
        assertThat(YoutubeParser.Companion.getVideoUriFromThumbnail("https://img.youtube.com/vi/ohGXwX6zKow/maxresdefault.jpg"),
                Matchers.is("https://youtube.com/watch?v=ohGXwX6zKow"));
        assertThat(YoutubeParser.Companion.getVideoUriFromThumbnail("https://img.youtube.com/vi/ohGXwX6zKow/0.jpg"),
                Matchers.is("https://youtube.com/watch?v=ohGXwX6zKow"));
    }

    private Message makeMessage(String text) {
        Message m = new Message();
        m.setData(text);
        m.setMimeType(MimeType.INSTANCE.getTEXT_PLAIN());

        return m;
    }
}
