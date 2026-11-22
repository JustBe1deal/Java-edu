package coursehub.practice6;

import java.math.*;
import java.util.*;
/** Запуск демонстрации лабораторной работы 6. */
public final class Practice6 {
    private Practice6() {}
    public static void main(String[] args) {
        String a = "Java", b = "Ja" + "va", c = new String("Java");
        System.out.println("literal == constant: " + (a == b) + "; literal == new: " + (a == c) + "; equals: " + a.equals(c));
        System.out.println("Код: " + CourseCode.parse("JAVA-101"));
        System.out.println("Дата: " + DateParser.parseDate("start", "29.02.2024"));
        Money price = new Money(new BigDecimal("100.00"), Currency.getInstance("RUB"));
        System.out.println("Доли: " + price.allocate(3));
    }
}
