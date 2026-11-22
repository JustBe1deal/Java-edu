package coursehub.practice4;

import java.util.*;
/** Демонстрирует цепочку вызовов и порядок try, catch и finally. */
public final class ExceptionExamples {
    private ExceptionExamples() {}
    public static int first(int value) { return second(value); }
    private static int second(int value) { return third(value); }
    private static int third(int value) { return fourth(value); }
    private static int fourth(int value) { if (value <= 0) throw new IllegalArgumentException("значение должно быть положительным"); return value; }
    public static String finallyExample(boolean fail, List<String> log) {
        try { log.add("try"); if (fail) throw new IllegalStateException("ошибка"); return "try result"; }
        catch (IllegalStateException e) { log.add("catch"); return "catch result"; }
        finally { log.add("finally"); }
    }
}
