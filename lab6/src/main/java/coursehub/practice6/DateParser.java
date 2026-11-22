package coursehub.practice6;

import java.time.*;
import java.time.format.*;
import java.util.*;
/** Строго разбирает текстовые даты и сохраняет причину ошибки. */
public final class DateParser {
    private DateParser() {}
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    /** Строго разбирает дату и добавляет имя поля в ошибку. */
    public static LocalDate parseDate(String field, String value) {
        Objects.requireNonNull(field); Objects.requireNonNull(value);
        try { return LocalDate.parse(value, DATE); }
        catch (DateTimeParseException e) { throw new InvalidDateFieldException(field, value, e); }
    }
}
