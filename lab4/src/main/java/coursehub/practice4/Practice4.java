package coursehub.practice4;

import java.util.*;
/** Запуск демонстрации лабораторной работы 4. */
public final class Practice4 {
    private Practice4() {}
    public static void main(String[] args) {
        try {
            ExceptionExamples.first(0); // специально ошибочный аргумент проходит по четырём методам
        } catch (IllegalArgumentException e) {
            e.printStackTrace(System.out);
        }
        System.out.println("Исправленный результат: " + ExceptionExamples.first(1));
        List<String> events = new ArrayList<>();
        System.out.println("Возврат: " + ExceptionExamples.finallyExample(true, events) + "; порядок: " + events);
    }
}
