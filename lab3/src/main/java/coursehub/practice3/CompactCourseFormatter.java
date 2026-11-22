package coursehub.practice3;

public final class CompactCourseFormatter implements CourseFormatter {
    @Override public String format(Course c) { return c.title(); }
}
