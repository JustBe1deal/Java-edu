package coursehub.practice3;

import java.util.*;
/** Запуск демонстрации лабораторной работы 3. */
public final class Practice3 {
    private Practice3() {}
    public static void main(String[] args) {
        Course c = new Course(1, "Java", 72);
        CourseFormatter[] fs = {new CompactCourseFormatter(), new DetailedCourseFormatter(), new UpperCourseFormatter()};
        System.out.println(Formatting.formatAll(c, fs));
        AbstractNotificationSender sender = new EmailSender(); Object text = "hello";
        System.out.println(sender.describe(text)); System.out.println(sender.describe("hello"));
        List<String> log = new ArrayList<>();
        MetricsSender inner = new MetricsSender(new EmailSender());
        NotificationSender loggedMetrics = new LoggingSender(inner, log::add);
        System.out.println(loggedMetrics.send("student@example.org", "Привет"));
        MetricsSender metricsOutside = new MetricsSender(new LoggingSender(new EmailSender(), log::add));
        metricsOutside.send("student@example.org", "Привет");
        List<EnrollmentPolicy> rules = List.of(new AgePolicy(18), new PrerequisitePolicy("JAVA-101"), new CapacityPolicy(), new PaymentPolicy(), new ActiveCoursePolicy());
        Applicant a = new Applicant(20, Set.of("JAVA-101"), true, true, true);
        System.out.println(EnrollmentPolicies.describe(new EnrollmentService(EnrollmentPolicies.allOf(rules)).enroll(a)));
        Offering offer = new Offering(DeliveryFormat.SELF_PACED, PaymentPlan.INSTALLMENTS);
        System.out.println(offer.delivery().name() + " / " + offer.payment().name());
    }
}
