package coursehub.practice3;

import java.util.*;
/** Применяет форматировщики курса через общий интерфейс. */
public final class Formatting {
    private Formatting() {}
    /** Вызывает каждый форматировщик для одного курса без проверки реализации. */
    public static List<String> formatAll(Course c, CourseFormatter[] formatters) {
        List<String> lines = new ArrayList<>();
        for (CourseFormatter formatter : formatters) lines.add(formatter.format(c));
        return List.copyOf(lines);
    }
}
