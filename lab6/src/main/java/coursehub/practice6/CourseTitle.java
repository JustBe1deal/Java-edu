package coursehub.practice6;

import java.util.*;
public record CourseTitle(String display, String searchKey) {
    public static CourseTitle of(String raw) {
        String display = Objects.requireNonNull(raw).strip();
        if (display.isEmpty()) throw new IllegalArgumentException("Пустое название");
        String key = display.replaceAll("(?U)\\s+", " ").toLowerCase(Locale.ROOT);
        return new CourseTitle(display, key);
    }
}
