package coursehub.practice7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** Проверяет строки каталога и собирает результаты импорта. */
public final class CatalogImporter {
    private CatalogImporter() {}
    /** Публикует полный каталог только при отсутствии ошибок разбора. */
    public static ImportResult importAtomic(Path path) throws IOException {
        List<Course> temp = new ArrayList<>(); List<ImportError> errors = new ArrayList<>();
        long line = 0, valid = 0;
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String s; while ((s = in.readLine()) != null) {
                line++;
                try { temp.add(TextCatalog.parse(s)); valid++; }
                catch (IllegalArgumentException e) { errors.add(new ImportError(line, e.getMessage())); }
            }
        }
        return new ImportResult(errors.isEmpty() ? temp : Collections.emptyList(), errors, line, valid, errors.size(), false);
    }
    /** Статистика потоковой проверки; корректные записи не накапливаются в памяти. */
    public static ImportResult scanLarge(Path path, int maxErrors, long stopAfterErrors) throws IOException {
        if (maxErrors < 0 || stopAfterErrors <= 0) throw new IllegalArgumentException("Некорректные лимиты");
        List<ImportError> errors = new ArrayList<>(); long lines = 0, valid = 0, bad = 0; boolean cancelled = false;
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String s; while ((s = in.readLine()) != null) {
                lines++;
                try { TextCatalog.parse(s); valid++; }
                catch (IllegalArgumentException e) {
                    bad++; if (errors.size() < maxErrors) errors.add(new ImportError(lines, e.getMessage()));
                    if (bad >= stopAfterErrors) { cancelled = true; break; }
                }
            }
        }
        return new ImportResult(Collections.emptyList(), errors, lines, valid, bad, cancelled);
    }
}
