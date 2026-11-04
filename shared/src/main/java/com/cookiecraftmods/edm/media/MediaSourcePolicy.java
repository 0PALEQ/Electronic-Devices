package com.cookiecraftmods.edm.media;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Server-owned rules for submitting and starting media. No network resolution is performed. */
public final class MediaSourcePolicy {
    private static final Map<String, String[]> HOSTS = new LinkedHashMap<>();
    static {
        HOSTS.put("youtube", new String[]{"youtube.com", "youtu.be", "youtube-nocookie.com"});
        HOSTS.put("twitch", new String[]{"twitch.tv"});
        HOSTS.put("kick", new String[]{"kick.com"});
        HOSTS.put("bilibili", new String[]{"bilibili.com", "b23.tv"});
        HOSTS.put("odysee", new String[]{"odysee.com"});
        HOSTS.put("dtube", new String[]{"d.tube"});
        HOSTS.put("vidlii", new String[]{"vidlii.com"});
        HOSTS.put("twitter", new String[]{"twitter.com", "x.com"});
        HOSTS.put("bluesky", new String[]{"bsky.app"});
        HOSTS.put("tiktok", new String[]{"tiktok.com"});
        HOSTS.put("facebook", new String[]{"facebook.com", "fb.watch"});
        HOSTS.put("instagram", new String[]{"instagram.com"});
        HOSTS.put("soundcloud", new String[]{"soundcloud.com"});
        HOSTS.put("newgrounds", new String[]{"newgrounds.com"});
        HOSTS.put("imgur", new String[]{"imgur.com"});
        HOSTS.put("lightshot", new String[]{"prnt.sc", "prntscr.com"});
        HOSTS.put("medal", new String[]{"medal.tv"});
        HOSTS.put("streamable", new String[]{"streamable.com"});
        HOSTS.put("sendvid", new String[]{"sendvid.com"});
        HOSTS.put("googleDrive", new String[]{"drive.google.com", "docs.google.com"});
        HOSTS.put("dropbox", new String[]{"dropbox.com", "dropboxusercontent.com"});
        HOSTS.put("mediafire", new String[]{"mediafire.com"});
        HOSTS.put("adult_content", new String[]{
            "pornhub.com", "pornhub.org", "xvideos.com", "xnxx.com", "xhamster.com",
            "redtube.com", "youporn.com", "tube8.com", "spankbang.com", "eporner.com",
            "chaturbate.com", "stripchat.com", "livejasmin.com", "onlyfans.com", "fansly.com"
        });
    }

    public static final class Rule {
        public boolean enabled = true;
        public boolean requireAdmin = false;
    }

    public Map<String, Rule> sources = defaults();
    /** Additional adult-site domains, without schemes or paths. Subdomains are included. */
    public List<String> additionalAdultDomains = new ArrayList<>();

    private static Map<String, Rule> defaults() {
        Map<String, Rule> rules = new LinkedHashMap<>();
        for (String source : HOSTS.keySet()) rules.put(source, new Rule());
        for (String source : new String[]{"direct", "file", "water"}) rules.put(source, new Rule());
        return rules;
    }

    public void sanitize() {
        if (sources == null) sources = defaults();
        // Migrate the former platform-specific setting without losing its restrictions.
        Rule legacy = sources.remove("pornhub");
        if (legacy != null) {
            Rule adult = sources.get("adult_content");
            if (adult == null) adult = new Rule();
            adult.enabled &= legacy.enabled;
            adult.requireAdmin |= legacy.requireAdmin;
            sources.put("adult_content", adult);
        }
        if (additionalAdultDomains == null) additionalAdultDomains = new ArrayList<>();
        additionalAdultDomains.removeIf(domain -> domain == null || domain.isBlank());
        additionalAdultDomains.replaceAll(domain -> domain.trim().toLowerCase(Locale.ROOT));
        for (String source : defaults().keySet()) {
            if (sources.get(source) == null) sources.put(source, new Rule());
        }
    }

    public static String sourceOf(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (scheme.equals("file") || scheme.equals("water")) return scheme;
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        for (Map.Entry<String, String[]> entry : HOSTS.entrySet()) {
            for (String domain : entry.getValue()) {
                if (matchesDomain(host, domain)) return entry.getKey();
            }
        }
        return "direct";
    }

    private static boolean matchesDomain(String host, String domain) {
        if (domain.endsWith(".")) domain = domain.substring(0, domain.length() - 1);
        return host.equals(domain) || host.endsWith("." + domain);
    }

    public void check(String url, int maxLength, boolean administrator) {
        String validated = MediaUrl.validate(url, maxLength);
        String source = sourceOf(validated);
        URI uri = URI.create(validated);
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
            if (additionalAdultDomains != null) {
                for (String domain : additionalAdultDomains) {
                    if (domain != null && !domain.isBlank() && matchesDomain(host, domain.trim().toLowerCase(Locale.ROOT))) {
                        source = "adult_content";
                        break;
                    }
                }
            }
        }
        Rule rule = sources == null ? null : sources.get(source);
        if (rule == null) rule = new Rule();
        if (!rule.enabled) throw new IllegalArgumentException("The server has disabled the " + source + " media source.");
        if (rule.requireAdmin && !administrator) {
            throw new IllegalArgumentException("Only server administrators may use the " + source + " media source.");
        }
    }
}
