package com.cookiecraftmods.edm.media;

/** Run with java -ea after compiling with the two shared media classes. */
public final class MediaWhitelistTest {
    private static final String LINK = "https://www.youtube.com/watch?v=approved";
    private static void denied(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection");
    }
    public static void main(String[] args) {
        MediaSourcePolicy policy = new MediaSourcePolicy();
        policy.check(LINK, 2048, false);
        policy.linkWhitelistMode = true;
        denied(() -> policy.check(LINK, 2048, false));
        denied(() -> policy.check(LINK, 2048, true));
        policy.approvedLinks.add(LINK);
        policy.check(LINK, 2048, false);
        denied(() -> policy.check(LINK + "&other=1", 2048, true));
        denied(() -> policy.check("https://www.youtube.com/watch?v=other", 2048, false));
        policy.sources.get("youtube").enabled = false;
        denied(() -> policy.check(LINK, 2048, true));
        policy.sources.get("youtube").enabled = true;
        policy.approvedLinks.remove(LINK);
        denied(() -> policy.check(LINK, 2048, true));
        policy.approvedLinks.add(null);
        policy.approvedLinks.add("invalid");
        policy.approvedLinks.add(LINK);
        policy.approvedLinks.add(LINK);
        policy.sanitize();
        if (!policy.approvedLinks.equals(java.util.List.of(LINK))) throw new AssertionError("Invalid sanitization");
        policy.approvedLinks = null;
        policy.sanitize();
        if (!policy.approvedLinks.isEmpty()) throw new AssertionError("Null migration failed");
        System.out.println("Whitelist security checks passed.");
    }
}
