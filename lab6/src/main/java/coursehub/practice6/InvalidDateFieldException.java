package coursehub.practice6;

public final class InvalidDateFieldException extends IllegalArgumentException {
    public InvalidDateFieldException(String field, String value, Throwable cause) { super("Неверная дата поля " + field + ": " + value, cause); }
}
