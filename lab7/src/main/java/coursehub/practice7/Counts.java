package coursehub.practice7;

/** Количество строк и единиц UTF-16 в обработанном тексте. */
public final class Counts {
    public final long lines, chars;
    Counts(long lines, long chars) { this.lines = lines; this.chars = chars; }
    @Override public String toString() { return "Строк: " + lines + ", символов UTF-16: " + chars; }
}
