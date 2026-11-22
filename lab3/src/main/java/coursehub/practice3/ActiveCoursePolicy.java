package coursehub.practice3;

public final class ActiveCoursePolicy implements EnrollmentPolicy {
    @Override public EnrollmentResult evaluate(Applicant a) { return a.active() ? new Accepted() : new Rejected("курс закрыт"); }
}
