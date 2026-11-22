package coursehub.practice5;

import java.util.*;
public interface Repository<ID,T> {
    void save(ID id, T entity); Optional<T> findById(ID id); List<T> findAll(); boolean deleteById(ID id);
}
