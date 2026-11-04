package com.cookiecraftmods.edm.media;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MediaUrlTest {
    private static final int MAX = 2048;

    @Test void acceptsEveryAdvertisedPlatformWithoutAHostAllowlist() {
        for (String host : new String[] {
            "youtube.com", "youtu.be", "twitch.tv", "kick.com", "bilibili.com", "odysee.com",
            "d.tube", "vidlii.com", "twitter.com", "x.com", "bsky.app", "tiktok.com",
            "facebook.com", "instagram.com", "soundcloud.com", "newgrounds.com", "imgur.com",
            "prnt.sc", "medal.tv", "streamable.com", "sendvid.com", "drive.google.com",
            "dropbox.com", "mediafire.com", "pornhub.com", "new-platform.example"
        }) {
            String url = "https://" + host + "/media?id=123&token=a%2Fb";
            assertEquals(url, MediaUrl.validate(url, MAX), host);
        }
    }

    @Test void acceptsDirectMediaStreamsPicturesAndPlaylists() {
        for (String extension : new String[] {"mp4", "mp3", "png", "gif", "webp", "svg", "m3u", "m3u8", "mpd"}) {
            String url = "https://cdn.example:8443/media." + extension + "?signature=a%2Bb";
            assertEquals(url, MediaUrl.validate(url, MAX));
        }
        assertEquals("http://localhost:8080/live", MediaUrl.validate("http://localhost:8080/live", MAX));
    }

    @Test void acceptsClientLocalFilesAndWaterResources() {
        for (String url : new String[] {"file:///C:/media/video.mp4", "file:///home/player/picture.png",
            "file://media-server/share/song.mp3", "water://local/video.mp4", "water://remote/video.mp4"}) {
            assertEquals(url, MediaUrl.validate(url, MAX));
        }
    }

    @Test void trimsAndPreservesSignedUrls() {
        assertEquals("https://cdn.example/media?sig=a%2Bb%2Fc&x=1#fragment",
            MediaUrl.validate("  https://cdn.example/media?sig=a%2Bb%2Fc&x=1#fragment  ", MAX));
        assertEquals("https://cdn.example/caf%C3%A9.png",
            MediaUrl.validate("https://cdn.example/caf\u00e9.png", MAX));
    }

    @Test void rejectsMalformedRelativeOrUnsupportedLocations() {
        for (String url : new String[] {"", "not a URL", "/home/video.mp4", "C:\\media\\video.mp4",
            "https:///missing-host", "https://example.com:99999/video", "https://example.com/a b",
            "https://user:password@example.com/video", "javascript:alert(1)", "data:video/mp4,abc",
            "file:relative.mp4", "file:///", "water:///missing-host", "https://example.com/%zz"}) {
            assertThrows(IllegalArgumentException.class, () -> MediaUrl.validate(url, MAX), url);
        }
        assertThrows(IllegalArgumentException.class, () -> MediaUrl.validate(null, MAX));
    }

    @Test void limitsBothInputAndAsciiEncodedLength() {
        String url = "https://cdn.example/video.mp4";
        assertEquals(url, MediaUrl.validate(url, url.length()));
        assertThrows(IllegalArgumentException.class, () -> MediaUrl.validate(url, url.length() - 1));
        String unicode = "https://cdn.example/\u00e9";
        assertThrows(IllegalArgumentException.class, () -> MediaUrl.validate(unicode, unicode.length()));
    }
}
