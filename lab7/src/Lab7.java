import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.CRC32;

/** Лабораторная 7: каталог курсов и примеры работы с файловым вводом-выводом. */
public class Lab7 {
    private static final int MAGIC = 0x434F5552; // ASCII COUR
    private static final int MAX_RECORDS = 1_000_000;

    /** Неизменяемая запись; разделители запрещены, чтобы текстовый формат был однозначен. */
    public static final class Course {
        public final int id;
        public final String title;
        public final int hours;
        public Course(int id, String title, int hours) {
            if (id <= 0 || hours <= 0 || title == null || title.isBlank()
                    || title.indexOf('|') >= 0 || title.indexOf('\n') >= 0 || title.indexOf('\r') >= 0)
                throw new IllegalArgumentException("Некорректный курс");
            this.id = id; this.title = title; this.hours = hours;
        }
        @Override public String toString() { return id + "|" + title + "|" + hours; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Course)) return false;
            Course c = (Course) o;
            return id == c.id && hours == c.hours && title.equals(c.title);
        }
        @Override public int hashCode() { return Objects.hash(id, title, hours); }
    }
    /** Строка формата id|название|часы. */
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
    public static String readString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    /** Результат подсчёта: символы Java (UTF-16 code units), включая переводы строк. */
    public static final class Counts {
        public final long lines, chars;
        Counts(long lines, long chars) { this.lines = lines; this.chars = chars; }
        @Override public String toString() { return "Строк: " + lines + ", символов UTF-16: " + chars; }
    }
    // 2. Копируем посимвольно, чтобы сохранить исходные окончания строк.
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
    // 3. Обёртки записывают событие только после фактического close ресурса.
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
    public static long countLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { return lines.count(); }
    }
    // 5. Формат: magic, version=1, count, затем id / modified UTF / hours.
    private static void writeRecord(DataOutputStream out, Course c) throws IOException {
        out.writeInt(c.id); out.writeUTF(c.title); out.writeInt(c.hours);
    }
    private static Course readRecord(DataInputStream in) throws IOException {
        try { return new Course(in.readInt(), in.readUTF(), in.readInt()); }
        catch (IllegalArgumentException e) { throw new IOException("Некорректная запись", e); }
    }
    private static int count(DataInputStream in) throws IOException {
        int n = in.readInt();
        if (n < 0 || n > MAX_RECORDS) throw new IOException("Некорректное число записей: " + n);
        return n;
    }
    public static void writeBinary(Path path, List<Course> courses) throws IOException {
        if (courses.size() > MAX_RECORDS) throw new IOException("Слишком много записей");
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(MAGIC); out.writeInt(1); out.writeInt(courses.size());
            for (Course c : courses) writeRecord(out, c);
        }
    }
    public static List<Course> readBinary(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != MAGIC) throw new IOException("Неверный magic");
            int version = in.readInt();
            if (version != 1) throw new IOException("Неизвестная версия: " + version);
            int n = count(in); List<Course> result = new ArrayList<>();
            for (int i = 0; i < n; i++) result.add(readRecord(in));
            if (in.read() != -1) throw new IOException("Лишние данные после записей");
            return List.copyOf(result);
        } catch (EOFException e) { throw new IOException("Обрезанный бинарный файл", e); }
    }
    /** Результат move сообщает, удалось ли выполнить именно атомарную замену. */
    public enum MoveResult { ATOMIC, NON_ATOMIC }
    public static MoveResult saveReplace(Path target, List<Course> courses) throws IOException {
        Path absolute = target.toAbsolutePath(); Path parent = absolute.getParent();
        Path temp = Files.createTempFile(parent, ".courses-", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (Course c : courses) { writer.write(c.toString()); writer.newLine(); }
                writer.flush();
            }
            try {
                Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                return MoveResult.ATOMIC;
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
                return MoveResult.NON_ATOMIC;
            }
        } finally { Files.deleteIfExists(temp); }
    }
    public static final class ImportError {
        public final long line; public final String message;
        ImportError(long line, String message) { this.line = line; this.message = message; }
        @Override public String toString() { return "Строка " + line + ": " + message; }
    }
    /** Все записи доступны только при полном успехе; ошибки и статистика доступны всегда. */
    public static final class ImportResult {
        public final List<Course> courses; public final List<ImportError> errors;
        public final long lines, valid, errorCount; public final boolean cancelled;
        ImportResult(List<Course> courses, List<ImportError> errors, long lines, long valid, long errorCount, boolean cancelled) {
            this.courses = List.copyOf(courses); this.errors = List.copyOf(errors);
            this.lines = lines; this.valid = valid; this.errorCount = errorCount; this.cancelled = cancelled;
        }
        public boolean success() { return errorCount == 0 && !cancelled; }
    }
    // 7. All-or-nothing: временные данные не возвращаются при наличии ошибок.
    public static ImportResult importAtomic(Path path) throws IOException {
        List<Course> temp = new ArrayList<>(); List<ImportError> errors = new ArrayList<>();
        long line = 0, valid = 0;
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String s; while ((s = in.readLine()) != null) {
                line++;
                try { temp.add(parse(s)); valid++; }
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
                try { parse(s); valid++; }
                catch (IllegalArgumentException e) {
                    bad++; if (errors.size() < maxErrors) errors.add(new ImportError(lines, e.getMessage()));
                    if (bad >= stopAfterErrors) { cancelled = true; break; }
                }
            }
        }
        return new ImportResult(Collections.emptyList(), errors, lines, valid, bad, cancelled);
    }
    // 9. Отдельный формат: magic, version, payload length, payload, CRC32(payload).
    // v1 payload: count + id/title; v2 payload: count + id/title/hours.
    public static void writeVersioned(Path path, List<Course> courses, int version) throws IOException {
        if (version != 1 && version != 2) throw new IOException("Неизвестная версия: " + version);
        if (courses.size() > MAX_RECORDS) throw new IOException("Слишком много записей");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream payload = new DataOutputStream(bytes)) {
            payload.writeInt(courses.size());
            for (Course c : courses) { payload.writeInt(c.id); payload.writeUTF(c.title); if (version == 2) payload.writeInt(c.hours); }
        }
        byte[] data = bytes.toByteArray();
        if (data.length > 64 * 1024 * 1024) throw new IOException("Слишком большой payload");
        CRC32 crc = new CRC32(); crc.update(data);
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(MAGIC); out.writeInt(version); out.writeInt(data.length); out.write(data); out.writeInt((int) crc.getValue());
        }
    }
    public static List<Course> readVersioned(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != MAGIC) throw new IOException("Неверный magic");
            int version = in.readInt();
            if (version != 1 && version != 2) throw new IOException("Неизвестная версия: " + version);
            int length = in.readInt();
            if (length < 4 || length > 64 * 1024 * 1024) throw new IOException("Некорректный размер payload");
            byte[] data = new byte[length]; in.readFully(data); int storedCrc = in.readInt();
            if (in.read() != -1) throw new IOException("Лишние данные");
            CRC32 crc = new CRC32(); crc.update(data);
            if ((int) crc.getValue() != storedCrc) throw new IOException("Контрольная сумма не совпадает");
            List<Course> result = new ArrayList<>();
            try (DataInputStream payload = new DataInputStream(new ByteArrayInputStream(data))) {
                int n = count(payload);
                for (int i = 0; i < n; i++) {
                    int id = payload.readInt(); String title = payload.readUTF();
                    int hours = version == 1 ? 1 : payload.readInt(); // миграция: часы по умолчанию
                    try { result.add(new Course(id, title, hours)); }
                    catch (IllegalArgumentException e) { throw new IOException("Некорректный курс", e); }
                }
                if (payload.read() != -1) throw new IOException("Лишние данные в payload");
            }
            return List.copyOf(result);
        } catch (EOFException e) { throw new IOException("Обрезанный файл", e); }
    }
    public static void main(String[] args) throws Exception {
        Path dir = args.length == 0 ? Paths.get("lab7-output") : Paths.get(args[0]);
        Files.createDirectories(dir);
        List<Course> courses = Arrays.asList(new Course(1, "Java", 72), new Course(2, "Алгоритмы", 48));
        Path txt = dir.resolve("courses.txt"); writeString(txt, courses);
        System.out.println("Каталог:\n" + readString(txt));
        System.out.println(copyBuffered(txt, dir.resolve("copy.txt")));
        System.out.println("Pipeline: " + pipelineFailure(txt, dir.resolve("pipeline.txt")));
        System.out.println("Ленивые строки: " + countLines(txt));
        try { brokenLines(txt).count(); } catch (IllegalStateException e) { System.out.println("Закрытый stream: " + e); }
        Path binary = dir.resolve("courses.bin"); writeBinary(binary, courses);
        System.out.println("Бинарный каталог: " + readBinary(binary));
        System.out.println("Замена: " + saveReplace(txt, courses));
        System.out.println("Импорт успешен: " + importAtomic(txt).success());
        ImportResult scan = scanLarge(txt, 10, 100);
        System.out.println("Потоковая проверка: " + scan.lines + " строк, ошибок " + scan.errorCount);
        Path versioned = dir.resolve("versioned.bin"); writeVersioned(versioned, courses, 2);
        System.out.println("Версия 2: " + readVersioned(versioned));
    }
}
