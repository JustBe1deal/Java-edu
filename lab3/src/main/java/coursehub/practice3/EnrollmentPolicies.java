package coursehub.practice3;

import java.util.*;
/** Составляет правила допуска и отображает их результаты. */
public final class EnrollmentPolicies {
    private EnrollmentPolicies() {}
    public static String describe(EnrollmentResult result) {
        return switch (result) {
            case Accepted ignored -> "Зачислен";
            case Rejected rejected -> "Отказ: " + rejected.reason();
            case WaitListed waiting -> "Ожидание: " + waiting.reason();
        };
    }
    /** Все правила должны принять заявку; ожидание возвращается при отсутствии отказа. */
    public static EnrollmentPolicy allOf(List<EnrollmentPolicy> policies) {
        List<EnrollmentPolicy> copy = List.copyOf(policies);
        return a -> { EnrollmentResult wait = null; for (EnrollmentPolicy p : copy) {
            EnrollmentResult r = Objects.requireNonNull(p.evaluate(a));
            if (r instanceof Rejected) return r; if (r instanceof WaitListed && wait == null) wait = r;
        } return wait == null ? new Accepted() : wait; };
    }
    /** anyOf принимает при первом успехе, иначе возвращает waitlist, затем последний отказ. */
    public static EnrollmentPolicy anyOf(List<EnrollmentPolicy> policies) {
        List<EnrollmentPolicy> copy = List.copyOf(policies);
        return a -> { EnrollmentResult wait = null, last = new Rejected("нет подходящего правила"); for (EnrollmentPolicy p : copy) {
            EnrollmentResult r = Objects.requireNonNull(p.evaluate(a));
            if (r instanceof Accepted) return r; if (r instanceof WaitListed && wait == null) wait = r; last = r;
        } return wait == null ? last : wait; };
    }
}
