package coursehub.practice7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
/** Демонстрирует закрытие потоков и обработку ленивых строк. */
public final class ResourceExamples {
    private ResourceExamples() {}
    /** Показывает порядок закрытия reader и writer при исключении. */
    public static List<String> pipelineFailure(Path source, Path target) throws IOException {
        List<String> events = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
                 @Override public void close() throws IOException { super.close(); events.add("close reader"); }
             };
             BufferedWriter out = new BufferedWriter(Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                 @Override public void close() throws IOException { super.close(); events.add("close writer"); }
             }) {
            int first = in.read();
            if (first != -1) out.write(first);
            throw new IOException("Исключение внутри pipeline");
        } catch (IOException e) {
            if (!"Исключение внутри pipeline".equals(e.getMessage()) || e.getSuppressed().length != 0) throw e;
            events.add("caught " + e.getMessage());
        }
        return events;
    }
    // 4. Этот метод намеренно демонстрирует ошибку: поток закрыт до возвращения.
    public static Stream<String> brokenLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { return lines; }
    }
    /** Обрабатывает ленивый поток строк внутри try-with-resources. */
    public static long countLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { return lines.count(); }
    }
}
