package coursehub.practice6;

import java.util.*;
/** Определяет допустимость null при работе с Integer. */
public final class IntegerPolicy {
    private IntegerPolicy() {}
    /** Отклоняет null до автоматического преобразования в int. */
    public static int requireInteger(Integer value) { return Objects.requireNonNull(value, "Число обязательно"); }
}
