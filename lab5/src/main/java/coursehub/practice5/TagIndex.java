package coursehub.practice5;

import java.util.*;
public final class TagIndex {
    private final Map<String, Set<Integer>> tags = new LinkedHashMap<>();
    private final Map<Integer, Course> courses = new LinkedHashMap<>();
    public void add(Course course) {
        Objects.requireNonNull(course);
        if (courses.containsKey(course.id())) throw new IllegalArgumentException("Повторный id");
        courses.put(course.id(), course);
        for (String tag : course.tags()) tags.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).add(course.id());
    }
    public void remove(int id) {
        Course course = courses.remove(id); if (course == null) return;
        for (String tag : course.tags()) { Set<Integer> ids = tags.get(tag); ids.remove(id); if (ids.isEmpty()) tags.remove(tag); }
    }
    public Set<Integer> intersection(Set<String> requested) {
        if (requested.isEmpty()) return Set.copyOf(courses.keySet());
        Set<Integer> found = new LinkedHashSet<>(courses.keySet());
        for (String tag : requested) found.retainAll(tags.getOrDefault(tag, Set.of()));
        return Collections.unmodifiableSet(found);
    }
}
