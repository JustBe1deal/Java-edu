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
            List<Lab7.Course> cs = Arrays.asList(new Lab7.Course(1, "Алгоритмы", 32), new Lab7.Course(2, "Java", 64));
            Path t = d.resolve("text.txt"); Lab7.writeString(t, cs);
            check(Lab7.readString(t).contains("Алгоритмы"), "UTF-8");
            fails(IOException.class, () -> Lab7.readString(d.resolve("missing")));
            fails(IOException.class, () -> Lab7.readString(d));
            fails(IOException.class, () -> Lab7.writeString(d, cs));
            Files.writeString(t, "А\r\nБ\nВ\rГ", StandardCharsets.UTF_8);
            Lab7.Counts counts = Lab7.copyBuffered(t, d.resolve("copy"));
            check(counts.lines == 4 && counts.chars == 8, "Подсчёт строк и символов");
            check(Files.readString(t).equals(Files.readString(d.resolve("copy"))), "Точное копирование");
            check(Lab7.pipelineFailure(t, d.resolve("out")).equals(Arrays.asList("close writer", "close reader", "caught Исключение внутри pipeline")), "Порядок close");
            check(Lab7.countLines(t) == 4, "Files.lines");
            fails(IllegalStateException.class, () -> Lab7.brokenLines(t).count());
            Path bin = d.resolve("data.bin"); Lab7.writeBinary(bin, cs);
            check(Lab7.readBinary(bin).equals(cs), "Бинарный round-trip");
            byte[] data = Files.readAllBytes(bin); Files.write(bin, Arrays.copyOf(data, data.length - 1));
            fails(IOException.class, () -> Lab7.readBinary(bin));
            Lab7.writeBinary(bin, cs); data = Files.readAllBytes(bin); data[7] = 9; Files.write(bin, data);
            fails(IOException.class, () -> Lab7.readBinary(bin));
            Lab7.saveReplace(t, cs); check(Lab7.importAtomic(t).courses.equals(cs), "Замена и импорт");
            Files.writeString(t, "1|Java|20\nошибка\n2|C|30\nневерно\n", StandardCharsets.UTF_8);
            Lab7.ImportResult result = Lab7.importAtomic(t);
            check(!result.success() && result.courses.isEmpty() && result.valid == 2 && result.errors.get(0).line == 2 && result.errors.get(1).line == 4, "Атомарный импорт");
            result = Lab7.scanLarge(t, 1, 2);
            check(result.cancelled && result.lines == 4 && result.valid == 2 && result.errorCount == 2 && result.errors.size() == 1, "Порог и лимит ошибок");
            Path v = d.resolve("versioned"); Lab7.writeVersioned(v, cs, 2);
            check(Lab7.readVersioned(v).equals(cs), "Версия 2");
            Lab7.writeVersioned(v, cs, 1);
            check(Lab7.readVersioned(v).equals(Arrays.asList(new Lab7.Course(1, "Алгоритмы", 1), new Lab7.Course(2, "Java", 1))), "Миграция v1");
            data = Files.readAllBytes(v); data[15] ^= 1; Files.write(v, data);
            fails(IOException.class, () -> Lab7.readVersioned(v));
            Lab7.writeVersioned(v, cs, 2); data = Files.readAllBytes(v); data[7] = 3; Files.write(v, data);
            fails(IOException.class, () -> Lab7.readVersioned(v));
            Lab7.writeVersioned(v, cs, 2); data = Files.readAllBytes(v); Files.write(v, Arrays.copyOf(data, data.length - 1));
            fails(IOException.class, () -> Lab7.readVersioned(v));
            System.out.println("Все проверки пройдены");
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(d)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.delete(p); } catch (IOException e) { throw new UncheckedIOException(e); } });
            }
        }
    }
}
