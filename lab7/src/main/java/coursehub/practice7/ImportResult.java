package coursehub.practice7;

import java.util.*;
/** Результат импорта и статистика; при ошибке частичный каталог не публикуется. */
public final class ImportResult {
    public final List<Course> courses; public final List<ImportError> errors;
    public final long lines, valid, errorCount; public final boolean cancelled;
    ImportResult(List<Course> courses, List<ImportError> errors, long lines, long valid, long errorCount, boolean cancelled) {
        this.courses = List.copyOf(courses); this.errors = List.copyOf(errors);
        this.lines = lines; this.valid = valid; this.errorCount = errorCount; this.cancelled = cancelled;
    }
    public boolean success() { return errorCount == 0 && !cancelled; }
}
