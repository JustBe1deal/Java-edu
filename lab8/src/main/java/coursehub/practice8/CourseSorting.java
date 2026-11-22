package coursehub.practice8;

import java.util.*;
/** Сортирует курсы по устойчивому набору критериев. */
public final class CourseSorting {
    private CourseSorting() {}
    public static final Comparator<Course> COURSE_ORDER = Comparator.comparing(Course::level)
        .thenComparingInt(Course::duration).thenComparing(Course::code)
        .thenComparing(Course::modules, CourseSorting::compareModules);
    private static int compareModules(List<String> a, List<String> b) {
        for (int i = 0; i < Math.min(a.size(),b.size()); i++) {
            int comparison = a.get(i).compareTo(b.get(i)); if (comparison != 0) return comparison;
        }
        return Integer.compare(a.size(),b.size());
    }
    /** Возвращает курсы в детерминированном порядке. */
    public static List<Course> sortedCourses(List<Course> courses) {
        return courses.stream().sorted(COURSE_ORDER).toList();
    }
}
