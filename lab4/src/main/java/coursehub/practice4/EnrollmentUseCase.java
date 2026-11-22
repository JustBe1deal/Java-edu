package coursehub.practice4;

import java.io.*;
import java.util.*;
/** Связывает импорт, поиск курса и проверку зачисления. */
public final class EnrollmentUseCase {
    private EnrollmentUseCase() {}
    /** Полностью импортирует каталог и затем обрабатывает заявку. */
    public static UseCaseResult importAndEnroll(Reader source, int courseId, boolean prerequisite, boolean duplicate) {
        // Programming error проверяется до границы внешних ошибок.
        Objects.requireNonNull(source, "source");
        if (courseId <= 0) throw new IllegalArgumentException("Некорректный courseId");
        try {
            List<Course> courses = CourseImporter.importCourses(source);
            Optional<Course> found = EnrollmentRules.findCourse(courses, courseId);
            if (found.isEmpty()) return new UseCaseResult("NOT_FOUND", null, null);
            Decision decision = EnrollmentRules.enroll(found.get(), prerequisite, duplicate);
            return decision.accepted() ? new UseCaseResult("ACCEPTED", found.get(), null) : new UseCaseResult(decision.reason().name(), null, null);
        } catch (CourseImportException e) {
            return new UseCaseResult(Reason.INVALID_DATA.name(), null, e);
        } catch (InfrastructureException e) {
            return new UseCaseResult(Reason.IO_ERROR.name(), null, e);
        }
    }
}
