package coursehub.practice4;

import java.util.*;
/** Проверяет правила зачисления и ищет курс в списке. */
public final class EnrollmentRules {
    private EnrollmentRules() {}
    public static final int MIN_HOURS = 1, MAX_HOURS = 1000;
    /** Проверяет условия зачисления в установленном порядке. */
    public static Decision enroll(Course c, boolean prerequisite, boolean duplicate) {
        Objects.requireNonNull(c);
        if (!c.isOpen()) return Decision.reject(Reason.CLOSED);
        if (duplicate) return Decision.reject(Reason.DUPLICATE);
        if (!prerequisite) return Decision.reject(Reason.PREREQUISITE);
        if (!c.hasSeats()) return Decision.reject(Reason.NO_SEATS);
        return Decision.accept();
    }
    /** Ищет курс по идентификатору и возвращает Optional. */
    public static Optional<Course> findCourse(List<Course> courses, int id) {
        return courses.stream().filter(c -> c.id() == id).findFirst();
    }
}
