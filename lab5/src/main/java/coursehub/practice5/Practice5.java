package coursehub.practice5;

import java.util.*;
/** Запуск демонстрации лабораторной работы 5. */
public final class Practice5 {
    private Practice5() {}
    public static void main(String[] args) {
        List<String> registrations = new ArrayList<>(List.of("Аня", "Борис", "Аня"));
        registrations.add(1, "Вера"); registrations.remove("Борис");
        System.out.println("Дубликаты: " + CollectionExercises.duplicates(registrations));
        Queue<String> waitlist = new ArrayDeque<>(); waitlist.offer("Аня"); waitlist.offer("Борис");
        System.out.println("Следующий: " + waitlist.peek() + "; извлечён: " + waitlist.poll());
        LruCache<String,Integer> cache = new LruCache<>(2); cache.put("A", 1); cache.put("B", 2); cache.get("A"); cache.put("C", 3);
        System.out.println("LRU: " + cache.keysOldestFirst());
        Graph<String> graph = new Graph<>(); graph.addVertex("Основы"); graph.addVertex("Java"); graph.addDependency("Java", "Основы");
        System.out.println("План: " + graph.plan());
    }
}
