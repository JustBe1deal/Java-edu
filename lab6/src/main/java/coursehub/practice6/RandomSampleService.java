package coursehub.practice6;

import java.util.*;
import java.util.random.RandomGenerator;
/** Генерирует выборку через переданный источник случайности. */
public final class RandomSampleService {
    private RandomSampleService() {}
    /** Создаёт выборку через переданный генератор в заданных границах. */
    public static List<Integer> randomSample(RandomGenerator random, int size, int lower, int upperExclusive) {
        if (size < 0 || lower >= upperExclusive) throw new IllegalArgumentException("Неверные границы");
        List<Integer> values = new ArrayList<>(); for (int i = 0; i < size; i++) values.add(random.nextInt(lower, upperExclusive)); return List.copyOf(values);
    }
}
