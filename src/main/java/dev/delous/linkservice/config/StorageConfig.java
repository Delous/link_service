package dev.delous.linkservice.config;

import java.nio.file.Path;

public class StorageConfig {
    public static final Path storageFile = Path.of("links.bin");
    public static final long expirationInterval = 86_400_000;
    public static final int clickLimit = 10;
}