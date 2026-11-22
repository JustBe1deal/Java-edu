package coursehub.practice4;

import java.util.*;
import java.util.regex.Pattern;
public record CourseCode(String value) {
    private static final Pattern VALID = Pattern.compile("[A-Z]{2,10}-[0-9]{3}");
    public CourseCode { value = Objects.requireNonNull(value).strip().toUpperCase(Locale.ROOT); if (!VALID.matcher(value).matches()) throw new IllegalArgumentException("Некорректный код"); }
}
