package coursehub.practice7;

import java.util.*;
/** Неизменяемая запись каталога с проверкой полей. */
public final class Course {
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
