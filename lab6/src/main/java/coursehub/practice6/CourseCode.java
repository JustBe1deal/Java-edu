package coursehub.practice6;

import java.util.*;
import java.util.regex.*;
public record CourseCode(String prefix, int number) {
    private static final Pattern PATTERN = Pattern.compile("([A-Z]{2,10})-([0-9]{3})");
    public static CourseCode parse(String text) {
        Matcher m = PATTERN.matcher(Objects.requireNonNull(text));
        if (!m.matches()) throw new IllegalArgumentException("Код: только ASCII A-Z и три цифры");
        return new CourseCode(m.group(1), Integer.parseInt(m.group(2)));
    }
}
