package coursehub.practice3;

import java.util.*;
public final class EnrollmentService {
    private final EnrollmentPolicy policy;
    public EnrollmentService(EnrollmentPolicy policy) { this.policy = Objects.requireNonNull(policy); }
    public EnrollmentResult enroll(Applicant applicant) { return policy.evaluate(applicant); }
}
