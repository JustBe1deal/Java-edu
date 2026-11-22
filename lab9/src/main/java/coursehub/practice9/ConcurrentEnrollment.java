package coursehub.practice9;

import java.util.*;
import java.util.concurrent.atomic.*;
/** Зачисление через атомарную замену неизменяемого снимка. */
public final class ConcurrentEnrollment implements Enrollment {
    private final int capacity;
    private final AtomicReference<Set<String>> state = new AtomicReference<>(Set.of());
    public ConcurrentEnrollment(int capacity) { if (capacity < 0) throw new IllegalArgumentException("capacity < 0"); this.capacity = capacity; }
    @Override public boolean enroll(String student) {
        Objects.requireNonNull(student);
        while (true) {
            Set<String> before = state.get();
            if (before.contains(student) || before.size() >= capacity) return false;
            Set<String> copy = new HashSet<>(before); copy.add(student);
            if (state.compareAndSet(before, Set.copyOf(copy))) return true;
            // Другой поток успел изменить снимок: повторяем проверку на новом.
        }
    }
    @Override public Set<String> students() { return state.get(); }
}
