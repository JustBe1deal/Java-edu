package coursehub.practice3;

import java.util.*;
public final class ReadOnlyCourses implements CourseReader {
    private final Map<Integer, Course> courses;
    public ReadOnlyCourses(Map<Integer, Course> courses) { this.courses = Map.copyOf(courses); }
    @Override public Optional<Course> find(int id) { return Optional.ofNullable(courses.get(id)); }
}
