package coursehub.practice5;

import java.util.*;
import java.util.function.Function;

/** Обобщённые операции со списками, наборами и индексами. */
public final class CollectionExercises {
    private CollectionExercises() {}
    /** Находит повторяющиеся элементы и сохраняет порядок их обнаружения. */
    public static <T> Set<T> duplicates(List<T> items) {
        Set<T> seen = new HashSet<>(), duplicates = new LinkedHashSet<>();
        for (T item : items) if (!seen.add(item)) duplicates.add(item);
        return duplicates;
    }
    /** Удаляет дубликаты, сохраняя порядок первого появления. */
    public static <T> List<T> uniqueInOrder(List<T> items) { return List.copyOf(new LinkedHashSet<>(items)); }
    /** Явный отказ при повторном ключе, даже если записи идентичны. */
    public static <T,K> Map<K,T> indexBy(List<T> items, Function<? super T, ? extends K> key) {
        Map<K,T> result = new LinkedHashMap<>();
        for (T item : items) {
            Objects.requireNonNull(item); K k = Objects.requireNonNull(key.apply(item));
            if (result.putIfAbsent(k, item) != null) throw new IllegalArgumentException("Повторный ключ: " + k);
        }
        return result;
    }
    /** Возвращает первый элемент или сообщает о пустом списке. */
    public static <T> T firstOrThrow(List<T> items) {
        if (items.isEmpty()) throw new NoSuchElementException("Пустой список"); return items.get(0);
    }
    /** Меняет местами элементы по указанным индексам. */
    public static <T> void swap(List<T> items, int a, int b) { Collections.swap(items, a, b); }
    /** Находит максимум элементов, сравнимых с T или его суперклассом. */
    public static <T extends Comparable<? super T>> T max(List<T> items) {
        if (items.isEmpty()) throw new NoSuchElementException("Пустой список");
        T best = Objects.requireNonNull(items.get(0));
        for (T item : items) if (Objects.requireNonNull(item).compareTo(best) > 0) best = item;
        return best;
    }
    /** Копирует элементы из producer-списка в consumer-список. */
    public static <T> void copy(List<? extends T> source, List<? super T> destination) {
        for (T item : source) destination.add(item);
    }
}
