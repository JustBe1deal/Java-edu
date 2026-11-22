package coursehub.practice3;

public final class CapacityPolicy implements EnrollmentPolicy {
    @Override public EnrollmentResult evaluate(Applicant a) { return a.hasSeat() ? new Accepted() : new WaitListed("нет мест"); }
}
