package coursehub.practice5;
import java.util.*;
import java.util.function.Function;

/** Примеры коллекций и обобщений; все операции выполняются в памяти. */
public final class Practice5 {
    private Practice5() {}
    public record CourseCode(String value) { public CourseCode { Objects.requireNonNull(value); } }
    public record Course(int id, String title, Set<String> tags) {
        public Course { Objects.requireNonNull(title); tags = Set.copyOf(tags); }
    }
    public static <T> Set<T> duplicates(List<T> items) {
        Set<T> seen = new HashSet<>(), duplicates = new LinkedHashSet<>();
        for (T item : items) if (!seen.add(item)) duplicates.add(item);
        return duplicates;
    }
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
    public static <T> T firstOrThrow(List<T> items) {
        if (items.isEmpty()) throw new NoSuchElementException("Пустой список"); return items.get(0);
    }
    public static <T> void swap(List<T> items, int a, int b) { Collections.swap(items, a, b); }
    public static <T extends Comparable<? super T>> T max(List<T> items) {
        if (items.isEmpty()) throw new NoSuchElementException("Пустой список");
        T best = Objects.requireNonNull(items.get(0));
        for (T item : items) if (Objects.requireNonNull(item).compareTo(best) > 0) best = item;
        return best;
    }
    public static <T> void copy(List<? extends T> source, List<? super T> destination) {
        for (T item : source) destination.add(item);
    }
    /** Результат содержит либо значение, либо сообщение об ошибке. */
    public static final class Result<T> {
        private final T value; private final String error;
        private Result(T value, String error) { this.value = value; this.error = error; }
        public static <T> Result<T> success(T value) { return new Result<>(Objects.requireNonNull(value), null); }
        public static <T> Result<T> failure(String error) { if (error == null || error.isBlank()) throw new IllegalArgumentException("Пустая ошибка"); return new Result<>(null, error); }
        public boolean isSuccess() { return error == null; }
        public T value() { if (!isSuccess()) throw new IllegalStateException(error); return value; }
        public Optional<String> error() { return Optional.ofNullable(error); }
    }
    public interface Repository<ID,T> {
        void save(ID id, T entity); Optional<T> findById(ID id); List<T> findAll(); boolean deleteById(ID id);
    }
    /** Повторный save заменяет значение; коллекция не раскрывается вызывающему коду. */
    public static final class MemoryRepository<ID,T> implements Repository<ID,T> {
        private final Map<ID,T> data = new LinkedHashMap<>();
        @Override public void save(ID id, T entity) { data.put(Objects.requireNonNull(id), Objects.requireNonNull(entity)); }
        @Override public Optional<T> findById(ID id) { return Optional.ofNullable(data.get(Objects.requireNonNull(id))); }
        @Override public List<T> findAll() { return List.copyOf(data.values()); }
        @Override public boolean deleteById(ID id) { return data.remove(Objects.requireNonNull(id)) != null; }
    }
    public static final class TagIndex {
        private final Map<String, Set<Integer>> tags = new LinkedHashMap<>();
        private final Map<Integer, Course> courses = new LinkedHashMap<>();
        public void add(Course course) {
            Objects.requireNonNull(course);
            if (courses.containsKey(course.id())) throw new IllegalArgumentException("Повторный id");
            courses.put(course.id(), course);
            for (String tag : course.tags()) tags.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).add(course.id());
        }
        public void remove(int id) {
            Course course = courses.remove(id); if (course == null) return;
            for (String tag : course.tags()) { Set<Integer> ids = tags.get(tag); ids.remove(id); if (ids.isEmpty()) tags.remove(tag); }
        }
        public Set<Integer> intersection(Set<String> requested) {
            if (requested.isEmpty()) return Set.copyOf(courses.keySet());
            Set<Integer> found = new LinkedHashSet<>(courses.keySet());
            for (String tag : requested) found.retainAll(tags.getOrDefault(tag, Set.of()));
            return Collections.unmodifiableSet(found);
        }
    }
    /** LinkedHashMap в режиме accessOrder перемещает прочитанный ключ в конец. */
    public static final class LruCache<K,V> {
        private final int capacity; private final LinkedHashMap<K,V> cache = new LinkedHashMap<>(16, .75f, true);
        public LruCache(int capacity) { if (capacity <= 0) throw new IllegalArgumentException("capacity > 0"); this.capacity = capacity; }
        public void put(K key, V value) {
            cache.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
            if (cache.size() > capacity) cache.remove(cache.keySet().iterator().next());
        }
        public Optional<V> get(K key) { return Optional.ofNullable(cache.get(Objects.requireNonNull(key))); }
        public List<K> keysOldestFirst() { return List.copyOf(cache.keySet()); }
    }
    /** Порядок вершин и рёбер совпадает с порядком добавления; отсутствующие вершины запрещены. */
    public static final class Graph<T> {
        private final Map<T, Set<T>> prerequisites = new LinkedHashMap<>();
        public void addVertex(T v) { prerequisites.putIfAbsent(Objects.requireNonNull(v), new LinkedHashSet<>()); }
        public void addDependency(T course, T prerequisite) {
            if (!prerequisites.containsKey(course) || !prerequisites.containsKey(prerequisite)) throw new IllegalArgumentException("Вершина отсутствует");
            prerequisites.get(course).add(prerequisite);
        }
        public List<T> plan() {
            Map<T,Integer> state = new HashMap<>(); List<T> path = new ArrayList<>(), order = new ArrayList<>();
            for (T v : prerequisites.keySet()) visit(v, state, path, order);
            return List.copyOf(order);
        }
        private void visit(T v, Map<T,Integer> state, List<T> path, List<T> order) {
            int s = state.getOrDefault(v, 0); if (s == 2) return;
            if (s == 1) { int begin = path.indexOf(v); List<T> cycle = new ArrayList<>(path.subList(begin, path.size())); cycle.add(v); throw new IllegalStateException("Цикл: " + cycle); }
            state.put(v, 1); path.add(v);
            for (T dependency : prerequisites.get(v)) visit(dependency, state, path, order);
            path.remove(path.size() - 1); state.put(v, 2); order.add(v);
        }
    }
    public static void main(String[] args) {
        List<String> registrations = new ArrayList<>(List.of("Аня", "Борис", "Аня"));
        registrations.add(1, "Вера"); registrations.remove("Борис");
        System.out.println("Дубликаты: " + duplicates(registrations));
        Queue<String> waitlist = new ArrayDeque<>(); waitlist.offer("Аня"); waitlist.offer("Борис");
        System.out.println("Следующий: " + waitlist.peek() + "; извлечён: " + waitlist.poll());
        LruCache<String,Integer> cache = new LruCache<>(2); cache.put("A", 1); cache.put("B", 2); cache.get("A"); cache.put("C", 3);
        System.out.println("LRU: " + cache.keysOldestFirst());
        Graph<String> graph = new Graph<>(); graph.addVertex("Основы"); graph.addVertex("Java"); graph.addDependency("Java", "Основы");
        System.out.println("План: " + graph.plan());
    }
}
