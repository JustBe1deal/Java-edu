package coursehub.practice4;

import java.io.*;
import java.util.*;
/** Разбирает записи и импортирует каталог из Reader. */
public final class CourseImporter {
    private CourseImporter() {}
    /** Разбирает одну запись и сохраняет номер и причину ошибки. */
    public static Course parseCourse(String line, long record) throws CourseImportException {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 2) throw new CourseImportException(record, new IllegalArgumentException("Ожидается id|duration"));
        try {
            int id = Integer.parseInt(fields[0]); int duration = Integer.parseInt(fields[1]);
            try { return new Course(id, duration, true, true); }
            catch (IllegalArgumentException e) { throw new CourseImportException(record, e); }
        } catch (NumberFormatException e) { throw new CourseImportException(record, e); }
    }
    /** Импорт владеет reader. Ни одна запись не возвращается до полного успеха и close. */
    public static List<Course> importCourses(Reader reader) throws CourseImportException, InfrastructureException {
        List<Course> staged = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(Objects.requireNonNull(reader))) {
            String line; long record = 0;
            while ((line = input.readLine()) != null) { record++; staged.add(parseCourse(line, record)); }
        } catch (IOException e) { throw new InfrastructureException(e); }
        return List.copyOf(staged);
    }
}
