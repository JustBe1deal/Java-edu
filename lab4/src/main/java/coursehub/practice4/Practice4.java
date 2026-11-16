package coursehub.practice4;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/** Домен и границы ошибок лабораторной работы 4. */
public final class Practice4 {
    private Practice4() {}
    public static final int MIN_HOURS = 1, MAX_HOURS = 1000;
    public enum Reason { INVALID_DATA, CLOSED, NO_SEATS, PREREQUISITE, DUPLICATE, IO_ERROR }
    public static final class Course {
        private final int id, duration; private int completed; private boolean open, hasSeats;
        public Course(int id, int duration, boolean open, boolean hasSeats) {
            if (id <= 0 || duration < MIN_HOURS || duration > MAX_HOURS) throw new IllegalArgumentException("Некорректная продолжительность или id");
            this.id = id; this.duration = duration; this.open = open; this.hasSeats = hasSeats;
        }
        public int id() { return id; } public int duration() { return duration; } public int completed() { return completed; }
        public void completeHours(int hours) {
            if (hours <= 0 || hours > duration - completed) throw new IllegalArgumentException("Часы вне допустимого диапазона");
            completed += hours;
        }
    }
    public record CourseCode(String value) {
        private static final Pattern VALID = Pattern.compile("[A-Z]{2,10}-[0-9]{3}");
        public CourseCode { value = Objects.requireNonNull(value).strip().toUpperCase(Locale.ROOT); if (!VALID.matcher(value).matches()) throw new IllegalArgumentException("Некорректный код"); }
    }
    public static final class EnrollmentRejectedException extends Exception {
        public final int studentId, courseId; public final Reason reason;
        public EnrollmentRejectedException(int studentId, int courseId, Reason reason, Throwable cause) {
            super("Зачисление отклонено: " + Objects.requireNonNull(reason), cause);
            this.studentId = studentId; this.courseId = courseId; this.reason = reason;
        }
    }
    public static final class CourseImportException extends Exception {
        public final long record;
        public CourseImportException(long record, Throwable cause) { super("Ошибка записи " + record, cause); this.record = record; }
    }
    public static final class InfrastructureException extends Exception {
        public InfrastructureException(Throwable cause) { super("Ошибка чтения", cause); }
    }
    public record Decision(boolean accepted, Reason reason) {
        public Decision { if (accepted == (reason != null)) throw new IllegalArgumentException("Некорректный результат"); }
        public static Decision accept() { return new Decision(true, null); }
        public static Decision reject(Reason reason) { return new Decision(false, Objects.requireNonNull(reason)); }
    }
    /** Приоритет отказов определён таблицей решений в README. */
    public static Decision enroll(Course c, boolean prerequisite, boolean duplicate) {
        Objects.requireNonNull(c);
        if (!c.open) return Decision.reject(Reason.CLOSED);
        if (duplicate) return Decision.reject(Reason.DUPLICATE);
        if (!prerequisite) return Decision.reject(Reason.PREREQUISITE);
        if (!c.hasSeats) return Decision.reject(Reason.NO_SEATS);
        return Decision.accept();
    }
    public static Optional<Course> findCourse(List<Course> courses, int id) {
        return courses.stream().filter(c -> c.id == id).findFirst();
    }
    public static Course parseCourse(String line, long record) throws CourseImportException {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 2) throw new CourseImportException(record, new IllegalArgumentException("Ожидается id|duration"));
        try {
            int id = Integer.parseInt(fields[0]); int duration = Integer.parseInt(fields[1]);
            try { return new Course(id, duration, true, true); }
            catch (IllegalArgumentException e) { throw new CourseImportException(record, e); }
        } catch (NumberFormatException e) { throw new CourseImportException(record, e); }
    }
    /** Импорт владеет reader. Ни одна запись не возвращается до полного успеха и close. */
    public static List<Course> importCourses(Reader reader) throws CourseImportException, InfrastructureException {
        List<Course> staged = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(Objects.requireNonNull(reader))) {
            String line; long record = 0;
            while ((line = input.readLine()) != null) { record++; staged.add(parseCourse(line, record)); }
        } catch (IOException e) { throw new InfrastructureException(e); }
        return List.copyOf(staged);
    }
    public static final class TracedResource implements AutoCloseable {
        private final List<String> log; private final boolean failClose;
        public TracedResource(List<String> log, boolean failClose) { this.log = log; this.failClose = failClose; log.add("open"); }
        public void use(boolean fail) throws IOException { log.add("use"); if (fail) throw new IOException("use failed"); }
        @Override public void close() throws IOException { log.add("close"); if (failClose) throw new IOException("close failed"); }
    }
    public static void traced(List<String> log, boolean failUse, boolean failClose) throws IOException {
        try (TracedResource r = new TracedResource(log, failClose)) { r.use(failUse); }
    }
    // Для чтения stack trace вызов идёт через четыре уровня; причина — неверное значение.
    public static int first(int value) { return second(value); }
    private static int second(int value) { return third(value); }
    private static int third(int value) { return fourth(value); }
    private static int fourth(int value) { if (value <= 0) throw new IllegalArgumentException("значение должно быть положительным"); return value; }
    public static String finallyExample(boolean fail, List<String> log) {
        try { log.add("try"); if (fail) throw new IllegalStateException("ошибка"); return "try result"; }
        catch (IllegalStateException e) { log.add("catch"); return "catch result"; }
        finally { log.add("finally"); }
    }
    /** Результат публичного use case не содержит частично импортированный каталог. */
    public record UseCaseResult(String code, Course course, Throwable diagnostic) {}
    public static UseCaseResult importAndEnroll(Reader source, int courseId, boolean prerequisite, boolean duplicate) {
        // Programming error проверяется до границы внешних ошибок.
        Objects.requireNonNull(source, "source");
        if (courseId <= 0) throw new IllegalArgumentException("Некорректный courseId");
        try {
            List<Course> courses = importCourses(source);
            Optional<Course> found = findCourse(courses, courseId);
            if (found.isEmpty()) return new UseCaseResult("NOT_FOUND", null, null);
            Decision decision = enroll(found.get(), prerequisite, duplicate);
            return decision.accepted() ? new UseCaseResult("ACCEPTED", found.get(), null) : new UseCaseResult(decision.reason().name(), null, null);
        } catch (CourseImportException e) {
            return new UseCaseResult(Reason.INVALID_DATA.name(), null, e);
        } catch (InfrastructureException e) {
            return new UseCaseResult(Reason.IO_ERROR.name(), null, e);
        }
    }
    public record PublicError(String code) {}
    /** Граница UI переводит только известные ошибки; programming errors не маскирует. */
    public static PublicError publicError(Exception error) {
        if (error instanceof CourseImportException) return new PublicError(Reason.INVALID_DATA.name());
        if (error instanceof InfrastructureException) return new PublicError(Reason.IO_ERROR.name());
        if (error instanceof EnrollmentRejectedException e) return new PublicError(e.reason.name());
        throw new IllegalArgumentException("Неизвестная ошибка", error);
    }
    /** Демонстрация stack trace и порядка finally; тесты запускаются отдельно Maven. */
    public static void main(String[] args) {
        try {
            first(0); // специально ошибочный аргумент проходит по четырём методам
        } catch (IllegalArgumentException e) {
            e.printStackTrace(System.out);
        }
        System.out.println("Исправленный результат: " + first(1));
        List<String> events = new ArrayList<>();
        System.out.println("Возврат: " + finallyExample(true, events) + "; порядок: " + events);
    }
}
