package coursehub.practice7;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;
/** Работа с версиями формата, контрольной суммой и миграцией. */
public final class VersionedCatalog {
    private VersionedCatalog() {}
    /** Запись и чтение версионированной схемы с проверкой CRC32. */
    public static void writeVersioned(Path path, List<Course> courses, int version) throws IOException {
        if (version != 1 && version != 2) throw new IOException("Неизвестная версия: " + version);
        if (courses.size() > BinaryFormatSupport.MAX_RECORDS) throw new IOException("Слишком много записей");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream payload = new DataOutputStream(bytes)) {
            payload.writeInt(courses.size());
            for (Course c : courses) { payload.writeInt(c.id); payload.writeUTF(c.title); if (version == 2) payload.writeInt(c.hours); }
        }
        byte[] data = bytes.toByteArray();
        if (data.length > 64 * 1024 * 1024) throw new IOException("Слишком большой payload");
        CRC32 crc = new CRC32(); crc.update(data);
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(BinaryFormatSupport.MAGIC); out.writeInt(version); out.writeInt(data.length); out.write(data); out.writeInt((int) crc.getValue());
        }
    }
    public static List<Course> readVersioned(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != BinaryFormatSupport.MAGIC) throw new IOException("Неверный magic");
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
                int n = BinaryFormatSupport.count(payload);
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
}
