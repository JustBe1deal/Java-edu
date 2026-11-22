package coursehub.practice7;

import java.nio.file.*;
import java.util.*;
/** Запуск демонстрации лабораторной работы 7. */
public final class Lab7 {
    private Lab7() {}
    public static void main(String[] args) throws Exception {
        Path dir = args.length == 0 ? Paths.get("lab7-output") : Paths.get(args[0]);
        Files.createDirectories(dir);
        List<Course> courses = Arrays.asList(new Course(1, "Java", 72), new Course(2, "Алгоритмы", 48));
        Path txt = dir.resolve("courses.txt"); TextCatalog.writeString(txt, courses);
        System.out.println("Каталог:\n" + TextCatalog.readString(txt));
        System.out.println(TextCatalog.copyBuffered(txt, dir.resolve("copy.txt")));
        System.out.println("Pipeline: " + ResourceExamples.pipelineFailure(txt, dir.resolve("pipeline.txt")));
        System.out.println("Ленивые строки: " + ResourceExamples.countLines(txt));
        try { ResourceExamples.brokenLines(txt).count(); } catch (IllegalStateException e) { System.out.println("Закрытый stream: " + e); }
        Path binary = dir.resolve("courses.bin"); BinaryCatalog.writeBinary(binary, courses);
        System.out.println("Бинарный каталог: " + BinaryCatalog.readBinary(binary));
        System.out.println("Замена: " + AtomicCatalogWriter.saveReplace(txt, courses));
        System.out.println("Импорт успешен: " + CatalogImporter.importAtomic(txt).success());
        ImportResult scan = CatalogImporter.scanLarge(txt, 10, 100);
        System.out.println("Потоковая проверка: " + scan.lines + " строк, ошибок " + scan.errorCount);
        Path versioned = dir.resolve("versioned.bin"); VersionedCatalog.writeVersioned(versioned, courses, 2);
        System.out.println("Версия 2: " + VersionedCatalog.readVersioned(versioned));
    }
}
