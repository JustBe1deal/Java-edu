package coursehub.practice5;

import java.util.*;
public final class MemoryRepository<ID,T> implements Repository<ID,T> {
    private final Map<ID,T> data = new LinkedHashMap<>();
    @Override public void save(ID id, T entity) { data.put(Objects.requireNonNull(id), Objects.requireNonNull(entity)); }
    @Override public Optional<T> findById(ID id) { return Optional.ofNullable(data.get(Objects.requireNonNull(id))); }
    @Override public List<T> findAll() { return List.copyOf(data.values()); }
    @Override public boolean deleteById(ID id) { return data.remove(Objects.requireNonNull(id)) != null; }
}
