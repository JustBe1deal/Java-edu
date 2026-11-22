package coursehub.practice7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Самостоятельные проверки без JUnit. При ошибке завершаются исключением. */
public class Lab7Test {
    interface Checked { void run() throws Exception; }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void fails(Class<? extends Exception> type, Checked action) throws Exception {
        try { action.run(); } catch (Exception e) { if (type.isInstance(e)) return; throw e; }
        throw new AssertionError("Ожидалось исключение " + type.getName());
    }
    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("lab7-test-");
        try {
            List<Course> cs = Arrays.asList(new Course(1, "Алгоритмы", 32), new Course(2, "Java", 64));
            Path t = d.resolve("text.txt"); TextCatalog.writeString(t, cs);
            check(TextCatalog.readString(t).contains("Алгоритмы"), "UTF-8");
            fails(IOException.class, () -> TextCatalog.readString(d.resolve("missing")));
            fails(IOException.class, () -> TextCatalog.readString(d));
            fails(IOException.class, () -> TextCatalog.writeString(d, cs));
            Files.writeString(t, "А\r\nБ\nВ\rГ", StandardCharsets.UTF_8);
            Counts counts = TextCatalog.copyBuffered(t, d.resolve("copy"));
            check(counts.lines == 4 && counts.chars == 8, "Подсчёт строк и символов");
            check(Files.readString(t).equals(Files.readString(d.resolve("copy"))), "Точное копирование");
            check(ResourceExamples.pipelineFailure(t, d.resolve("out")).equals(Arrays.asList("close writer", "close reader", "caught Исключение внутри pipeline")), "Порядок close");
            check(ResourceExamples.countLines(t) == 4, "Files.lines");
            fails(IllegalStateException.class, () -> ResourceExamples.brokenLines(t).count());
            Path bin = d.resolve("data.bin"); BinaryCatalog.writeBinary(bin, cs);
            check(BinaryCatalog.readBinary(bin).equals(cs), "Бинарный round-trip");
            byte[] data = Files.readAllBytes(bin); Files.write(bin, Arrays.copyOf(data, data.length - 1));
            fails(IOException.class, () -> BinaryCatalog.readBinary(bin));
            BinaryCatalog.writeBinary(bin, cs); data = Files.readAllBytes(bin); data[7] = 9; Files.write(bin, data);
            fails(IOException.class, () -> BinaryCatalog.readBinary(bin));
            AtomicCatalogWriter.saveReplace(t, cs); check(CatalogImporter.importAtomic(t).courses.equals(cs), "Замена и импорт");
            Files.writeString(t, "1|Java|20\nошибка\n2|C|30\nневерно\n", StandardCharsets.UTF_8);
            ImportResult result = CatalogImporter.importAtomic(t);
            check(!result.success() && result.courses.isEmpty() && result.valid == 2 && result.errors.get(0).line == 2 && result.errors.get(1).line == 4, "Атомарный импорт");
            result = CatalogImporter.scanLarge(t, 1, 2);
            check(result.cancelled && result.lines == 4 && result.valid == 2 && result.errorCount == 2 && result.errors.size() == 1, "Порог и лимит ошибок");
            Path v = d.resolve("versioned"); VersionedCatalog.writeVersioned(v, cs, 2);
            check(VersionedCatalog.readVersioned(v).equals(cs), "Версия 2");
            VersionedCatalog.writeVersioned(v, cs, 1);
            check(VersionedCatalog.readVersioned(v).equals(Arrays.asList(new Course(1, "Алгоритмы", 1), new Course(2, "Java", 1))), "Миграция v1");
            data = Files.readAllBytes(v); data[15] ^= 1; Files.write(v, data);
            fails(IOException.class, () -> VersionedCatalog.readVersioned(v));
            VersionedCatalog.writeVersioned(v, cs, 2); data = Files.readAllBytes(v); data[7] = 3; Files.write(v, data);
            fails(IOException.class, () -> VersionedCatalog.readVersioned(v));
            VersionedCatalog.writeVersioned(v, cs, 2); data = Files.readAllBytes(v); Files.write(v, Arrays.copyOf(data, data.length - 1));
            fails(IOException.class, () -> VersionedCatalog.readVersioned(v));
            System.out.println("Все проверки пройдены");
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(d)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException e) { throw new UncheckedIOException(e); } });
            }
        }
    }
}
