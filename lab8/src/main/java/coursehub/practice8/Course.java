package coursehub.practice8;

import java.util.*;
public record Course(String code, Level level, int duration, List<String> modules) {
    public Course {
        if (code == null || code.isBlank() || level == null || duration <= 0) throw new IllegalArgumentException("Некорректный курс");
        modules = List.copyOf(modules);
    }
}
