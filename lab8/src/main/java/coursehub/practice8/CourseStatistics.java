package coursehub.practice8;

import java.util.*;
import java.util.stream.*;
/** Подсчитывает статистику и выбирает курсы через Stream API. */
public final class CourseStatistics {
    private CourseStatistics() {}
    public static Totals totals(List<Course> courses) {
        return courses.stream().map(c -> new Totals(1,c.duration())).reduce(new Totals(0,0), Totals::combine);
    }
    public static long totalDuration(List<Course> courses) { return courses.stream().mapToLong(Course::duration).reduce(0L, Math::addExact); }
    public static long wrongIdentity(List<Course> courses) { return courses.stream().mapToLong(Course::duration).reduce(10L, Math::addExact); }
    /** Считает количество и среднее для каждой группы уровня. */
    public static Map<Level, LevelSummary> byLevel(List<Course> courses) {
        return Collections.unmodifiableMap(courses.stream().collect(Collectors.groupingBy(Course::level,
            () -> new EnumMap<>(Level.class), Collectors.collectingAndThen(Collectors.summarizingInt(Course::duration),
                s -> new LevelSummary(s.getCount(),s.getSum(),s.getAverage())))));
    }
    /** Возвращает описание самого длинного курса или запасной текст. */
    public static String longestLabel(List<Course> courses) {
        // При равной длительности выбираем лексикографически меньший код.
        return courses.stream().sorted(Comparator.comparingInt(Course::duration).reversed().thenComparing(Course::code))
            .findFirst().map(c -> c.code() + " (" + c.duration() + " ч.)").orElse("Курсов нет");
    }
}
