package coursehub.practice7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** Читает, записывает и копирует текстовый каталог в UTF-8. */
public final class TextCatalog {
    private TextCatalog() {}
    /** Разбирает одну строку каталога в объект курса. */
    public static Course parse(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 3) throw new IllegalArgumentException("Ожидаются три поля id|название|часы");
        try { return new Course(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2])); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("id и часы должны быть целыми числами", e); }
    }
    private static String text(List<Course> courses) {
        StringBuilder b = new StringBuilder();
        for (Course c : courses) b.append(c).append('\n');
        return b.toString();
    }
    // 1. Исключения намеренно передаются вызывающему коду: успех не имитируется.
    public static void writeString(Path path, List<Course> courses) throws IOException {
        Files.writeString(path, text(courses), StandardCharsets.UTF_8);
    }
    /** Читает текстовый файл целиком в UTF-8. */
    public static String readString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    /** Копирует текст и считает строки и единицы UTF-16, включая переводы строк. */
    public static Counts copyBuffered(Path source, Path target) throws IOException {
        if (source.toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize()))
            throw new IllegalArgumentException("Исходный и целевой пути совпадают");
        long chars = 0, lines = 0; boolean previousCR = false, hasLast = false; int last = -1;
        try (BufferedReader in = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             BufferedWriter out = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = in.read()) != -1) {
                out.write(ch); chars++; hasLast = true; last = ch;
                if (ch == '\r') { lines++; previousCR = true; }
                else { if (ch == '\n' && !previousCR) lines++; previousCR = false; }
            }
        }
        if (hasLast && last != '\r' && last != '\n') lines++;
        return new Counts(lines, chars);
    }
}
