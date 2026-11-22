package coursehub.practice5;

import java.util.*;
public final class Graph<T> {
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
