package coursehub.practice3;

public final class DetailedCourseFormatter implements CourseFormatter {
    @Override public String format(Course c) { return c.id() + ": " + c.title() + ", " + c.hours() + " ч."; }
}
