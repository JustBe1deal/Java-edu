package coursehub.practice8;

import java.util.*;
import java.util.stream.*;
/** Нормализует темы и строит обратный индекс через flatMap. */
public final class TopicIndex {
    private TopicIndex() {}
    /** Приводит тему к поисковому виду с фиксированной локалью. */
    public static String normalize(String topic) {
        return Objects.requireNonNull(topic).strip().replaceAll("(?U)\\s+", " ").toLowerCase(Locale.ROOT);
    }
    public static Set<String> topics(List<Course> courses) {
        TreeSet<String> result = courses.stream().flatMap(c -> c.modules().stream())
            .map(TopicIndex::normalize).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(TreeSet::new));
        return Collections.unmodifiableSet(result);
    }
    /** flatMap создаёт поток пар тема/код, а не список списков. */
    public static Map<String, Set<String>> topicIndex(List<Course> courses) {
        Map<String, Set<String>> index = courses.stream()
            .flatMap(c -> c.modules().stream().map(TopicIndex::normalize).filter(s -> !s.isEmpty())
                .map(topic -> Map.entry(topic, c.code())))
            .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(TreeSet::new))));
        index.replaceAll((k,v) -> Collections.unmodifiableSet(v));
        return Collections.unmodifiableMap(index);
    }
}
