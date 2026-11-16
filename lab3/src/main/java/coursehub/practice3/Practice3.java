package coursehub.practice3;

import java.util.*;
import java.util.function.Consumer;

/** Лабораторная 3: интерфейсы, подстановка, композиция и стратегии. */
public final class Practice3 {
    private Practice3() {}
    public record Course(int id, String title, int hours) {}
    public interface CourseFormatter { String format(Course course); }
    public static final class CompactCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) { return c.title(); }
    }
    public static final class DetailedCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) { return c.id() + ": " + c.title() + ", " + c.hours() + " ч."; }
    }
    public static final class UpperCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) { return c.title().toUpperCase(Locale.ROOT); }
    }
    /** Клиент не проверяет конкретные типы форматировщиков. */
    public static List<String> formatAll(Course c, CourseFormatter[] formatters) {
        List<String> lines = new ArrayList<>();
        for (CourseFormatter formatter : formatters) lines.add(formatter.format(c));
        return List.copyOf(lines);
    }
    public interface NotificationSender { String send(String recipient, String message); }
    public static abstract class AbstractNotificationSender implements NotificationSender {
        @Override public final String send(String recipient, String message) {
            if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("Пустой адресат");
            return doSend(recipient, Objects.requireNonNull(message));
        }
        protected abstract String doSend(String recipient, String message);
        public String describe(Object value) { return "object:" + value; }
        public String describe(String value) { return "string:" + value; }
    }
    public static final class EmailSender extends AbstractNotificationSender {
        @Override protected String doSend(String recipient, String message) { return "email " + recipient + ": " + message; }
        @Override public String describe(Object value) { return "email-object:" + value; }
    }
    public static final class ConsoleSender extends AbstractNotificationSender {
        @Override protected String doSend(String recipient, String message) { return "console " + recipient + ": " + message; }
    }
    /** Декораторы можно комбинировать без создания подкласса на каждую комбинацию. */
    public static final class LoggingSender implements NotificationSender {
        private final NotificationSender delegate; private final Consumer<String> log;
        public LoggingSender(NotificationSender delegate, Consumer<String> log) { this.delegate = Objects.requireNonNull(delegate); this.log = Objects.requireNonNull(log); }
        @Override public String send(String to, String message) { log.accept("before"); try { return delegate.send(to, message); } finally { log.accept("after"); } }
    }
    public static final class MetricsSender implements NotificationSender {
        private final NotificationSender delegate; public int attempts; public int failures;
        public MetricsSender(NotificationSender delegate) { this.delegate = Objects.requireNonNull(delegate); }
        @Override public String send(String to, String message) { attempts++; try { return delegate.send(to, message); } catch (RuntimeException e) { failures++; throw e; } }
    }
    public interface CourseReader { Optional<Course> find(int id); }
    public interface CourseWriter { void save(Course course); }
    public interface CourseRepository extends CourseReader, CourseWriter {}
    public static final class ReadOnlyCourses implements CourseReader {
        private final Map<Integer, Course> courses;
        public ReadOnlyCourses(Map<Integer, Course> courses) { this.courses = Map.copyOf(courses); }
        @Override public Optional<Course> find(int id) { return Optional.ofNullable(courses.get(id)); }
    }
    public record Applicant(int age, Set<String> completed, boolean hasSeat, boolean paid, boolean active) {
        public Applicant { completed = Set.copyOf(completed); }
    }
    public sealed interface EnrollmentResult permits Accepted, Rejected, WaitListed {}
    public record Accepted() implements EnrollmentResult {}
    public record Rejected(String reason) implements EnrollmentResult {}
    public record WaitListed(String reason) implements EnrollmentResult {}
    public static String describe(EnrollmentResult result) {
        return switch (result) {
            case Accepted ignored -> "Зачислен";
            case Rejected rejected -> "Отказ: " + rejected.reason();
            case WaitListed waiting -> "Ожидание: " + waiting.reason();
        };
    }
    @FunctionalInterface public interface EnrollmentPolicy { EnrollmentResult evaluate(Applicant applicant); }
    public static final class AgePolicy implements EnrollmentPolicy {
        private final int min; public AgePolicy(int min) { this.min = min; }
        @Override public EnrollmentResult evaluate(Applicant a) { return a.age() >= min ? new Accepted() : new Rejected("возраст"); }
    }
    public static final class PrerequisitePolicy implements EnrollmentPolicy {
        private final String required; public PrerequisitePolicy(String required) { this.required = required; }
        @Override public EnrollmentResult evaluate(Applicant a) { return a.completed().contains(required) ? new Accepted() : new Rejected("prerequisite"); }
    }
    public static final class CapacityPolicy implements EnrollmentPolicy {
        @Override public EnrollmentResult evaluate(Applicant a) { return a.hasSeat() ? new Accepted() : new WaitListed("нет мест"); }
    }
    public static final class PaymentPolicy implements EnrollmentPolicy {
        @Override public EnrollmentResult evaluate(Applicant a) { return a.paid() ? new Accepted() : new Rejected("нет оплаты"); }
    }
    public static final class ActiveCoursePolicy implements EnrollmentPolicy {
        @Override public EnrollmentResult evaluate(Applicant a) { return a.active() ? new Accepted() : new Rejected("курс закрыт"); }
    }
    /** allOf возвращает первый отказ; waitlist имеет приоритет только при отсутствии отказов. */
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
    public static final class EnrollmentService {
        private final EnrollmentPolicy policy;
        public EnrollmentService(EnrollmentPolicy policy) { this.policy = Objects.requireNonNull(policy); }
        public EnrollmentResult enroll(Applicant applicant) { return policy.evaluate(applicant); }
    }
    public interface FirstGreeting { default String greeting() { return "первый"; } }
    public interface SecondGreeting { default String greeting() { return "второй"; } }
    public static final class Greeting implements FirstGreeting, SecondGreeting {
        @Override public String greeting() { return FirstGreeting.super.greeting(); }
    }
    // Независимые оси: добавление формата или оплаты не создаёт декартово произведение классов.
    public interface Delivery { String name(); }
    public interface Payment { String name(); }
    public enum DeliveryFormat implements Delivery { ONLINE, CLASSROOM, HYBRID, SELF_PACED }
    public enum PaymentPlan implements Payment { FREE, FIXED, SUBSCRIPTION, INSTALLMENTS }
    public record Offering(Delivery delivery, Payment payment) {}
    public static List<Offering> combinations() {
        List<Offering> offerings = new ArrayList<>();
        for (DeliveryFormat format : DeliveryFormat.values())
            for (PaymentPlan payment : PaymentPlan.values()) offerings.add(new Offering(format, payment));
        return List.copyOf(offerings);
    }
    public static void main(String[] args) {
        Course c = new Course(1, "Java", 72);
        CourseFormatter[] fs = {new CompactCourseFormatter(), new DetailedCourseFormatter(), new UpperCourseFormatter()};
        System.out.println(formatAll(c, fs));
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
        System.out.println(describe(new EnrollmentService(allOf(rules)).enroll(a)));
        Offering offer = new Offering(DeliveryFormat.SELF_PACED, PaymentPlan.INSTALLMENTS);
        System.out.println(offer.delivery().name() + " / " + offer.payment().name());
    }
}
