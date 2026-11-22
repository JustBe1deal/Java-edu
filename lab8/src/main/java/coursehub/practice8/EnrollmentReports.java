package coursehub.practice8;

import java.util.*;
import java.util.stream.*;
/** Строит однопроходный отчёт по событиям зачисления. */
public final class EnrollmentReports {
    private EnrollmentReports() {}
    private static void addAnomaly(TreeSet<String> retained, String message, int limit) {
        if (limit == 0) return;
        retained.add(message); if (retained.size() > limit) retained.pollLast();
    }
    /** Поток обрабатывается ровно один раз; параллельный stream намеренно запрещён. */
    public static Report report(Stream<EnrollmentEvent> events, List<Course> catalog, int maxAnomalies) {
        Objects.requireNonNull(events); Objects.requireNonNull(catalog);
        if (events.isParallel() || maxAnomalies < 0) throw new IllegalArgumentException("Нужен последовательный поток и неотрицательный лимит");
        Map<String, Course> courses = new TreeMap<>();
        for (Course course : catalog) if (courses.putIfAbsent(course.code(),course) != null) throw new IllegalArgumentException("Повторный код: " + course.code());
        Map<Pair,Boolean> pairs = new HashMap<>(); Set<String> students = new HashSet<>();
        TreeSet<String> anomalies = new TreeSet<>(); long total = 0, anomalyCount = 0;
        // Внешний ресурс потока принадлежит вызывающему коду, который открывает и закрывает stream.
        Iterator<EnrollmentEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            EnrollmentEvent event = Objects.requireNonNull(iterator.next()); total++;
            if (!courses.containsKey(event.courseCode())) {
                anomalyCount++; addAnomaly(anomalies,"Неизвестный курс: " + event.courseCode(),maxAnomalies); continue;
            }
            students.add(event.student()); Pair key = new Pair(event.student(),event.courseCode());
            Boolean previous = pairs.putIfAbsent(key,event.completed());
            if (previous != null) {
                anomalyCount++; addAnomaly(anomalies,"Повтор: " + event.student() + " / " + event.courseCode(),maxAnomalies);
                if (event.completed() && !previous) pairs.put(key,true);
            }
        }
        Map<String,long[]> counts = new TreeMap<>(); for (String code : courses.keySet()) counts.put(code,new long[2]);
        pairs.forEach((pair,completed) -> { long[] a = counts.get(pair.course()); a[0]++; if (completed) a[1]++; });
        List<CourseResult> results = courses.values().stream().map(c -> new CourseResult(c.code(),c.level(),counts.get(c.code())[0],counts.get(c.code())[1])).toList();
        List<CourseResult> top = results.stream().sorted(Comparator.comparingLong(CourseResult::enrolled).reversed().thenComparing(CourseResult::code)).limit(3).toList();
        Map<Level,Long> levels = new EnumMap<>(Level.class); for (Level level : Level.values()) levels.put(level,0L);
        for (CourseResult r : results) levels.merge(r.level(),r.enrolled(),Long::sum);
        return new Report(total,students.size(),students.stream().sorted().toList(),results,top,Collections.unmodifiableMap(levels),anomalyCount,List.copyOf(anomalies));
    }
}
