package coursehub.practice4;

import java.io.*;
import java.util.*;
/** Показывает использование и закрытие тестового ресурса. */
public final class ResourceDemo {
    private ResourceDemo() {}
    public static void traced(List<String> log, boolean failUse, boolean failClose) throws IOException {
        try (TracedResource r = new TracedResource(log, failClose)) { r.use(failUse); }
    }
    // Для чтения stack trace вызов идёт через четыре уровня; причина — неверное значение.
}
