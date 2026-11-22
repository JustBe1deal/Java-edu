package coursehub.practice7;

import java.io.*;
import java.nio.file.*;
import java.util.*;
/** Читает и записывает простой бинарный каталог версии 1. */
public final class BinaryCatalog {
    private BinaryCatalog() {}
    private static void writeRecord(DataOutputStream out, Course c) throws IOException {
        out.writeInt(c.id); out.writeUTF(c.title); out.writeInt(c.hours);
    }
    private static Course readRecord(DataInputStream in) throws IOException {
        try { return new Course(in.readInt(), in.readUTF(), in.readInt()); }
        catch (IllegalArgumentException e) { throw new IOException("Некорректная запись", e); }
    }
    /** Записывает заголовок и записи в простой бинарный формат. */
    public static void writeBinary(Path path, List<Course> courses) throws IOException {
        if (courses.size() > BinaryFormatSupport.MAX_RECORDS) throw new IOException("Слишком много записей");
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(BinaryFormatSupport.MAGIC); out.writeInt(1); out.writeInt(courses.size());
            for (Course c : courses) writeRecord(out, c);
        }
    }
    /** Проверяет заголовок, версию и полноту бинарного каталога. */
    public static List<Course> readBinary(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != BinaryFormatSupport.MAGIC) throw new IOException("Неверный magic");
            int version = in.readInt();
            if (version != 1) throw new IOException("Неизвестная версия: " + version);
            int n = BinaryFormatSupport.count(in); List<Course> result = new ArrayList<>();
            for (int i = 0; i < n; i++) result.add(readRecord(in));
            if (in.read() != -1) throw new IOException("Лишние данные после записей");
            return List.copyOf(result);
        } catch (EOFException e) { throw new IOException("Обрезанный бинарный файл", e); }
    }
}
