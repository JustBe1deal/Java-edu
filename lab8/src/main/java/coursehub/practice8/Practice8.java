package coursehub.practice8;

import java.util.*;
import java.util.stream.*;
/** Запуск демонстрации лабораторной работы 8. */
public final class Practice8 {
    private Practice8() {}
    public static void main(String[] args) {
        List<Course> courses = List.of(new Course("JAVA-101",Level.BEGINNER,40,List.of(" Java ","Streams")),
            new Course("JAVA-201",Level.ADVANCED,60,List.of("streams","Threads")));
        System.out.println("Курсы: " + CourseSorting.sortedCourses(courses));
        System.out.println("Темы: " + TopicIndex.topicIndex(courses));
        System.out.println("Сумма: " + CourseStatistics.totals(courses) + ", статистика: " + courses.stream().collect(CourseStatsCollector.statsCollector()));
        System.out.println("Максимум: " + CourseStatistics.longestLabel(courses));
        try (Stream<EnrollmentEvent> events = Stream.of(new EnrollmentEvent("Аня","JAVA-101",true),new EnrollmentEvent("Борис","JAVA-201",false))) {
            System.out.println("Отчёт: " + EnrollmentReports.report(events,courses,10));
        }
    }
}
