package coursehub.practice3;

public final class PaymentPolicy implements EnrollmentPolicy {
    @Override public EnrollmentResult evaluate(Applicant a) { return a.paid() ? new Accepted() : new Rejected("нет оплаты"); }
}
