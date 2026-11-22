package coursehub.practice8;

import java.util.*;
public record Report(long events, long uniqueStudents, List<String> studentIds, List<CourseResult> courses,
                     List<CourseResult> top3, Map<Level,Long> levelEnrollments,
                     long anomalyCount, List<String> anomalies) {}
