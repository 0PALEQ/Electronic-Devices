package com.cookiecraftmods.edm.media;

import java.net.URI;
import java.util.Locale;

/** Validates media locations without resolving them or restricting platform hosts. */
public final class MediaUrl {
    private MediaUrl() {}

    public static String validate(String rawUrl, int maxLength) {
        String value = rawUrl == null ? "" : rawUrl.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Enter a media URL.");
        if (value.length() > maxLength) throw new IllegalArgumentException("The media URL is too long.");
        final URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("The media URL is invalid.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (uri.isOpaque() || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Use an absolute media URL without embedded login details.");
        }
        switch (scheme) {
            case "http", "https", "water" -> {
                if (uri.getHost() == null || uri.getHost().isBlank() || uri.getPort() > 65535) {
                    throw new IllegalArgumentException("The media URL must include a valid host.");
                }
            }
            case "file" -> {
                if (uri.getPath() == null || !uri.getPath().startsWith("/") || uri.getPath().equals("/")) {
                    throw new IllegalArgumentException("Use an absolute file URL, such as file:///C:/media/video.mp4.");
                }
            }
            default -> throw new IllegalArgumentException("Use an HTTP, HTTPS, file, or water media URL.");
        }
        String ascii = uri.toASCIIString();
        String normalized = scheme + ascii.substring(ascii.indexOf(':'));
        if (normalized.length() > maxLength) throw new IllegalArgumentException("The media URL is too long.");
        return normalized;
    }
}
