package coursehub.practice8;

import java.util.stream.*;
/** Создаёт коллектор статистики с корректным объединением частей. */
public final class CourseStatsCollector {
    private CourseStatsCollector() {}
    /** Создаёт коллектор с отдельным аккумулятором и immutable результатом. */
    public static Collector<Course, ?, Stats> statsCollector() {
        return Collector.of(StatsAccumulator::new, StatsAccumulator::add, StatsAccumulator::merge, StatsAccumulator::finish);
    }

}
