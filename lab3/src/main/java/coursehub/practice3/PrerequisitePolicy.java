package coursehub.practice3;

public final class PrerequisitePolicy implements EnrollmentPolicy {
    private final String required; public PrerequisitePolicy(String required) { this.required = required; }
    @Override public EnrollmentResult evaluate(Applicant a) { return a.completed().contains(required) ? new Accepted() : new Rejected("prerequisite"); }
}
