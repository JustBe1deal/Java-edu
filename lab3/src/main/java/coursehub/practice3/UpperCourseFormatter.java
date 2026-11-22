package coursehub.practice3;

import java.util.*;
public final class UpperCourseFormatter implements CourseFormatter {
    @Override public String format(Course c) { return c.title().toUpperCase(Locale.ROOT); }
}
