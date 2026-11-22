package coursehub.practice5;

import java.util.*;
public final class LruCache<K,V> {
    private final int capacity; private final LinkedHashMap<K,V> cache = new LinkedHashMap<>(16, .75f, true);
    public LruCache(int capacity) { if (capacity <= 0) throw new IllegalArgumentException("capacity > 0"); this.capacity = capacity; }
    public void put(K key, V value) {
        cache.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
        if (cache.size() > capacity) cache.remove(cache.keySet().iterator().next());
    }
    public Optional<V> get(K key) { return Optional.ofNullable(cache.get(Objects.requireNonNull(key))); }
    public List<K> keysOldestFirst() { return List.copyOf(cache.keySet()); }
}
