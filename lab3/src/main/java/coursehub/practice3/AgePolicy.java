package coursehub.practice3;

public final class AgePolicy implements EnrollmentPolicy {
    private final int min; public AgePolicy(int min) { this.min = min; }
    @Override public EnrollmentResult evaluate(Applicant a) { return a.age() >= min ? new Accepted() : new Rejected("возраст"); }
}
