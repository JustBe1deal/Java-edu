package coursehub.practice5;
import java.util.*;
/** Исполняемые эксперименты о коллекциях; время не является микробенчмарком. */
public final class ExtraExamples {
    private ExtraExamples() {}
    public static final class MutableKey {
        final int id; String title;
        MutableKey(int id,String title) { this.id=id; this.title=title; }
        @Override public boolean equals(Object o) { return o instanceof MutableKey k && id==k.id && title.equals(k.title); }
        @Override public int hashCode() { return Objects.hash(id,title); }
    }
    /** Название можно менять: равенство определяется только неизменяемым id. */
    public static final class StableCourseKey {
        final int id; String title;
        StableCourseKey(int id, String title) { this.id=id; this.title=title; }
        @Override public boolean equals(Object o) { return o instanceof StableCourseKey c && id == c.id; }
        @Override public int hashCode() { return Integer.hashCode(id); }
    }
    public record Rating(int value) implements Comparable<Rating> {
        @Override public int compareTo(Rating other) { return Integer.compare(value, other.value); }
    }
    public static void main(String[] args) {
        List<Integer> values=new ArrayList<>(List.of(1,2,3,4));
        try { for (int v: values) if(v==1) values.remove(Integer.valueOf(v)); }
        catch (ConcurrentModificationException e) { System.out.println("Ожидаемый fail-fast: " + e); }
        values=new ArrayList<>(List.of(1,2,3,4));
        for (Iterator<Integer> it=values.iterator();it.hasNext();) if(it.next()%2==0) it.remove();
        values.removeIf(v->v==1); System.out.println("После удаления: " + values);
        StableCourseKey stable = new StableCourseKey(1,"Java");
        Set<StableCourseKey> stableSet = new HashSet<>(); Map<StableCourseKey,String> stableMap = new HashMap<>();
        stableSet.add(stable); stableMap.put(stable,"данные"); stable.title="Новое название";
        System.out.println("Стабильный ключ: " + stableSet.contains(stable) + ", " + stableMap.get(stable));
        System.out.println("Максимумы: " + CollectionExercises.max(List.of(1,3,2)) + ", " + CollectionExercises.max(List.of("a","z")) + ", " + CollectionExercises.max(List.of(new Rating(1),new Rating(2))));
        List<Integer> source = List.of(1,2); List<Number> destination = new ArrayList<>();
        CollectionExercises.copy(source, destination); System.out.println("PECS: " + destination);
        Result<String> text = Result.success("готово");
        Result<Integer> number = Result.success(42);
        System.out.println("Результаты: " + text.value() + ", " + number.value());
        MutableKey key=new MutableKey(1,"Java"); Set<MutableKey> keys=new HashSet<>(); keys.add(key); key.title="C";
        System.out.println("Изменённый ключ найден: " + keys.contains(key));
        int n=10000; List<Integer> array=new ArrayList<>(), linked=new LinkedList<>();
        for(int i=0;i<n;i++){ array.add(i); linked.add(i); }
        measure("ArrayList",array); measure("LinkedList",linked);
    }
    private static void measure(String name,List<Integer> list) {
        long start=System.nanoTime(), sum=0;
        for(int i=0;i<list.size();i++) sum+=list.get(i);
        long indexed=System.nanoTime()-start; start=System.nanoTime();
        for(int value:list) sum+=value;
        long traversal=System.nanoTime()-start;
        System.out.println(name+": index="+indexed+" ns; iteration="+traversal+" ns; sum="+sum);
    }
}
