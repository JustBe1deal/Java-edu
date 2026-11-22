package coursehub.practice7;

public final class ImportError {
    public final long line; public final String message;
    ImportError(long line, String message) { this.line = line; this.message = message; }
    @Override public String toString() { return "Строка " + line + ": " + message; }
}
