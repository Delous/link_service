package dev.delous.linkservice.data;

import java.io.Serializable;
import java.util.UUID;

public class ShortLinkRecord implements Serializable {
    private UUID userUuid;
    private String originalUrl;
    private long expiresAt;
    private int maxClicks;
    private int clicks;

    public ShortLinkRecord(
            UUID userUuid,
            String originalUrl,
            long expiresAt,
            int maxClicks) {
        this.userUuid = userUuid;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.maxClicks = maxClicks;
        this.clicks = 0;
    }

    public boolean isExpired(long now) {
        return now >= expiresAt;
    }

    public boolean isClickLimitReached() {
        return clicks >= maxClicks;
    }

    public String getOriginalUrl(UUID uuid) {
        if (uuid == userUuid) {
            return originalUrl;
        }
        else return null;
    }

    public void addClick () {
        clicks++;
    }
}
