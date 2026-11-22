package coursehub.practice7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** Записывает каталог во временный файл и заменяет целевой файл. */
public final class AtomicCatalogWriter {
    private AtomicCatalogWriter() {}
    /** Закрывает временный файл до замены целевого файла. */
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
}
