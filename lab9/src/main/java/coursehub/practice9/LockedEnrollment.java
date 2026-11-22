package coursehub.practice9;

import java.util.*;
/** Зачисление под одной общей блокировкой. */
public final class LockedEnrollment implements Enrollment {
    private final int capacity; private final Set<String> students = new HashSet<>();
    public LockedEnrollment(int capacity) { if (capacity < 0) throw new IllegalArgumentException("capacity < 0"); this.capacity = capacity; }
    @Override public synchronized boolean enroll(String student) {
        Objects.requireNonNull(student);
        if (students.contains(student) || students.size() >= capacity) return false;
        return students.add(student);
    }
    @Override public synchronized Set<String> students() { return Set.copyOf(students); }
}
