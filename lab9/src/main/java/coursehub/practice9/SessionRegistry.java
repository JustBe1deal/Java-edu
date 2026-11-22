package coursehub.practice9;

import java.util.*;
import java.util.concurrent.*;
public final class SessionRegistry {
    private final ConcurrentMap<String,String> sessions = new ConcurrentHashMap<>();
    public boolean createIfAbsent(String id, String user) {
        return sessions.putIfAbsent(Objects.requireNonNull(id),Objects.requireNonNull(user)) == null;
    }
    public Optional<String> find(String id) { return Optional.ofNullable(sessions.get(Objects.requireNonNull(id))); }
    public boolean remove(String id, String user) { return sessions.remove(Objects.requireNonNull(id),Objects.requireNonNull(user)); }
}
