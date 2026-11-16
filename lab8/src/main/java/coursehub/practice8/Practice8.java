package coursehub.practice8;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/** Практическая работа 8: лямбды, Stream API и однопроходная статистика. */
public final class Practice8 {
    private Practice8() {}

    public enum Level { BEGINNER, INTERMEDIATE, ADVANCED }
    public record Course(String code, Level level, int duration, List<String> modules) {
        public Course {
            if (code == null || code.isBlank() || level == null || duration <= 0) throw new IllegalArgumentException("Некорректный курс");
            modules = List.copyOf(modules);
        }
    }
    // Все поля, различающие объекты, участвуют в последнем tie-breaker.
    public static final Comparator<Course> COURSE_ORDER = Comparator.comparing(Course::level)
        .thenComparingInt(Course::duration).thenComparing(Course::code)
        .thenComparing(Course::modules, Practice8::compareModules);
    private static int compareModules(List<String> a, List<String> b) {
        for (int i = 0; i < Math.min(a.size(),b.size()); i++) {
            int comparison = a.get(i).compareTo(b.get(i)); if (comparison != 0) return comparison;
        }
        return Integer.compare(a.size(),b.size());
    }
    public static List<Course> sortedCourses(List<Course> courses) {
        return courses.stream().sorted(COURSE_ORDER).toList();
    }
    public static String normalize(String topic) {
        return Objects.requireNonNull(topic).strip().replaceAll("(?U)\\s+", " ").toLowerCase(Locale.ROOT);
    }
    public static Set<String> topics(List<Course> courses) {
        TreeSet<String> result = courses.stream().flatMap(c -> c.modules().stream())
            .map(Practice8::normalize).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(TreeSet::new));
        return Collections.unmodifiableSet(result);
    }
    /** flatMap создаёт поток пар тема/код, а не список списков. */
    public static Map<String, Set<String>> topicIndex(List<Course> courses) {
        Map<String, Set<String>> index = courses.stream()
            .flatMap(c -> c.modules().stream().map(Practice8::normalize).filter(s -> !s.isEmpty())
                .map(topic -> Map.entry(topic, c.code())))
            .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(TreeSet::new))));
        index.replaceAll((k,v) -> Collections.unmodifiableSet(v));
        return Collections.unmodifiableMap(index);
    }
    public record Totals(long courses, long duration) {
        public Totals combine(Totals other) { return new Totals(Math.addExact(courses,other.courses), Math.addExact(duration,other.duration)); }
    }
    public static Totals totals(List<Course> courses) {
        return courses.stream().map(c -> new Totals(1,c.duration())).reduce(new Totals(0,0), Totals::combine);
    }
    public static long totalDuration(List<Course> courses) { return courses.stream().mapToLong(Course::duration).reduce(0L, Math::addExact); }
    public static long wrongIdentity(List<Course> courses) { return courses.stream().mapToLong(Course::duration).reduce(10L, Math::addExact); }
    public record LevelSummary(long count, long total, double average) {}
    public static Map<Level, LevelSummary> byLevel(List<Course> courses) {
        return Collections.unmodifiableMap(courses.stream().collect(Collectors.groupingBy(Course::level,
            () -> new EnumMap<>(Level.class), Collectors.collectingAndThen(Collectors.summarizingInt(Course::duration),
                s -> new LevelSummary(s.getCount(),s.getSum(),s.getAverage())))));
    }
    public static String longestLabel(List<Course> courses) {
        // При равной длительности выбираем лексикографически меньший код.
        return courses.stream().sorted(Comparator.comparingInt(Course::duration).reversed().thenComparing(Course::code))
            .findFirst().map(c -> c.code() + " (" + c.duration() + " ч.)").orElse("Курсов нет");
    }
    public record Stats(long count, long sum, OptionalInt min, OptionalInt max) {}
    private static final class StatsAccumulator {
        long count, sum; int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        void add(Course c) { count = Math.addExact(count,1); sum = Math.addExact(sum,c.duration()); min = Math.min(min,c.duration()); max = Math.max(max,c.duration()); }
        StatsAccumulator merge(StatsAccumulator b) { count = Math.addExact(count,b.count); sum = Math.addExact(sum,b.sum); min = Math.min(min,b.min); max = Math.max(max,b.max); return this; }
        Stats finish() { return new Stats(count,sum,count == 0 ? OptionalInt.empty() : OptionalInt.of(min),count == 0 ? OptionalInt.empty() : OptionalInt.of(max)); }
    }
    public static Collector<Course, ?, Stats> statsCollector() {
        return Collector.of(StatsAccumulator::new, StatsAccumulator::add, StatsAccumulator::merge, StatsAccumulator::finish);
    }

    /** Один event означает состояние заявки на пару студент/курс, completed объединяется через OR. */
    public record EnrollmentEvent(String student, String courseCode, boolean completed) {
        public EnrollmentEvent {
            if (student == null || student.isBlank() || courseCode == null || courseCode.isBlank()) throw new IllegalArgumentException("Некорректное событие");
        }
    }
    public record CourseResult(String code, Level level, long enrolled, long completed) {
        public double completionRate() { return enrolled == 0 ? 0.0 : (double) completed / enrolled; }
    }
    public record Report(long events, long uniqueStudents, List<String> studentIds, List<CourseResult> courses,
                         List<CourseResult> top3, Map<Level,Long> levelEnrollments,
                         long anomalyCount, List<String> anomalies) {}
    private record Pair(String student, String course) {}
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
    public static void main(String[] args) {
        List<Course> courses = List.of(new Course("JAVA-101",Level.BEGINNER,40,List.of(" Java ","Streams")),
            new Course("JAVA-201",Level.ADVANCED,60,List.of("streams","Threads")));
        System.out.println("Курсы: " + sortedCourses(courses));
        System.out.println("Темы: " + topicIndex(courses));
        System.out.println("Сумма: " + totals(courses) + ", статистика: " + courses.stream().collect(statsCollector()));
        System.out.println("Максимум: " + longestLabel(courses));
        try (Stream<EnrollmentEvent> events = Stream.of(new EnrollmentEvent("Аня","JAVA-101",true),new EnrollmentEvent("Борис","JAVA-201",false))) {
            System.out.println("Отчёт: " + report(events,courses,10));
        }
    }
}
