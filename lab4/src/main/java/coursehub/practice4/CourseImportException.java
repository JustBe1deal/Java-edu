package coursehub.practice4;

public final class CourseImportException extends Exception {
    public final long record;
    public CourseImportException(long record, Throwable cause) { super("Ошибка записи " + record, cause); this.record = record; }
}
