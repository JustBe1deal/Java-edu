package coursehub.practice5;

import java.util.*;
/** Обобщённый результат: либо значение, либо сообщение об ошибке. */
public final class Result<T> {
    private final T value; private final String error;
    private Result(T value, String error) { this.value = value; this.error = error; }
    public static <T> Result<T> success(T value) { return new Result<>(Objects.requireNonNull(value), null); }
    public static <T> Result<T> failure(String error) { if (error == null || error.isBlank()) throw new IllegalArgumentException("Пустая ошибка"); return new Result<>(null, error); }
    public boolean isSuccess() { return error == null; }
    public T value() { if (!isSuccess()) throw new IllegalStateException(error); return value; }
    public Optional<String> error() { return Optional.ofNullable(error); }
}
