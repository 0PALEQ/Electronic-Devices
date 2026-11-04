package com.cookiecraftmods.edm.media;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MediaSourcePolicyTest {
    @Test void recognizesAliasesAndSubdomainsWithoutMatchingLookalikes() {
        assertEquals("youtube", MediaSourcePolicy.sourceOf("https://m.youtube.com/watch?v=1"));
        assertEquals("youtube", MediaSourcePolicy.sourceOf("https://youtu.be/1"));
        assertEquals("youtube", MediaSourcePolicy.sourceOf("https://YOUTUBE.COM./watch?v=1"));
        assertEquals("twitter", MediaSourcePolicy.sourceOf("https://x.com/video/1"));
        assertEquals("direct", MediaSourcePolicy.sourceOf("https://notyoutube.com/video"));
        assertEquals("direct", MediaSourcePolicy.sourceOf("https://youtube.com.example/video"));
        assertEquals("file", MediaSourcePolicy.sourceOf("file:///C:/video.mp4"));
        assertEquals("water", MediaSourcePolicy.sourceOf("water://local/video.mp4"));
    }

    @Test void disabledSourcesRejectAdministratorsToo() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        policy.sources.get("youtube").enabled = false;
        for (boolean admin : new boolean[]{false, true}) {
            assertThrows(IllegalArgumentException.class, () -> policy.check("https://youtu.be/1", 2048, admin));
        }
        assertDoesNotThrow(() -> policy.check("https://twitch.tv/channel", 2048, false));
    }

    @Test void administratorRequirementIsSourceSpecific() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        for (String source : new String[]{"youtube", "direct", "file", "water"}) policy.sources.get(source).requireAdmin = true;
        for (String url : new String[]{"https://youtube.com/watch?v=1", "https://cdn.example/video.mp4", "file:///C:/video.mp4", "water://local/video.mp4"}) {
            assertThrows(IllegalArgumentException.class, () -> policy.check(url, 2048, false));
            assertDoesNotThrow(() -> policy.check(url, 2048, true));
        }
        assertDoesNotThrow(() -> policy.check("https://twitch.tv/channel", 2048, false));
    }

    @Test void upgradesMissingRulesWithoutOverwritingExistingRules() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        policy.sources.get("youtube").enabled = false;
        policy.sources.put("file", null);
        policy.sources.remove("water");
        policy.sanitize();
        assertFalse(policy.sources.get("youtube").enabled);
        assertTrue(policy.sources.get("file").enabled);
        assertTrue(policy.sources.get("water").enabled);
        policy.sources = null;
        policy.sanitize();
        assertNotNull(policy.sources.get("direct"));
    }

    @Test void policyNeverAcceptsInvalidUrls() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        for (String url : new String[]{"javascript:alert(1)", "https://user:pass@youtube.com/video", "not a url"}) {
            assertThrows(IllegalArgumentException.class, () -> policy.check(url, 2048, true));
        }
    }

    @Test void adultCategoryCoversMultipleSitesAndCustomDomains() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        policy.sources.get("adult_content").enabled = false;
        for (String url : new String[]{"https://www.xvideos.com/video", "https://m.xhamster.com/video"}) {
            assertEquals("adult_content", MediaSourcePolicy.sourceOf(url));
            assertThrows(IllegalArgumentException.class, () -> policy.check(url, 2048, true));
        }
        policy.additionalAdultDomains.add("adult-site.example");
        assertThrows(IllegalArgumentException.class, () -> policy.check("https://cdn.adult-site.example/video", 2048, true));
        assertDoesNotThrow(() -> policy.check("https://adult-site.example.other/video", 2048, false));
        policy.sources.get("adult_content").enabled = true;
        policy.sources.get("adult_content").requireAdmin = true;
        assertThrows(IllegalArgumentException.class, () -> policy.check("https://adult-site.example/video", 2048, false));
        assertDoesNotThrow(() -> policy.check("https://adult-site.example/video", 2048, true));
    }

    @Test void migratesLegacyRestrictionsWithoutWeakeningCategoryRules() {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        MediaSourcePolicy.Rule legacy = new MediaSourcePolicy.Rule();
        legacy.enabled = false;
        policy.sources.put("pornhub", legacy);
        policy.sources.get("adult_content").requireAdmin = true;
        policy.sanitize();
        assertFalse(policy.sources.containsKey("pornhub"));
        assertFalse(policy.sources.get("adult_content").enabled);
        assertTrue(policy.sources.get("adult_content").requireAdmin);
    }
}
