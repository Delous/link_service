package dev.delous.linkservice.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Storage<K extends Serializable, V extends Serializable> {

    private final Path file;
    private final Map<K, V> map = new HashMap<>();

    public Storage(Path file) {
        this.file = file;
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    public void remove(K key) {
        map.remove(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public void save() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");

            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
                out.writeObject(map);
            }

            Files.move(tmp, file,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save store to file: " + file, e);
        }
    }

    public void load() {
        if (!Files.exists(file)) return;

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof Map<?, ?> loaded) {
                map.clear();
                @SuppressWarnings("unchecked")
                Map<K, V> casted = (Map<K, V>) loaded;
                map.putAll(casted);
            }
        } catch (Exception e) {
            map.clear();
        }
    }
}
