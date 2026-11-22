package coursehub.practice4;

import java.util.*;
public record Decision(boolean accepted, Reason reason) {
    public Decision { if (accepted == (reason != null)) throw new IllegalArgumentException("Некорректный результат"); }
    public static Decision accept() { return new Decision(true, null); }
    public static Decision reject(Reason reason) { return new Decision(false, Objects.requireNonNull(reason)); }
}
