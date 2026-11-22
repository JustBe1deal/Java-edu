package coursehub.practice4;

import java.util.*;
public final class EnrollmentRejectedException extends Exception {
    public final int studentId, courseId; public final Reason reason;
    public EnrollmentRejectedException(int studentId, int courseId, Reason reason, Throwable cause) {
        super("Зачисление отклонено: " + Objects.requireNonNull(reason), cause);
        this.studentId = studentId; this.courseId = courseId; this.reason = reason;
    }
}
