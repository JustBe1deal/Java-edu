package coursehub.practice5;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class Practice5Test {
 @Test void duplicateIndexIsRejected(){ assertThrows(IllegalArgumentException.class,()->Practice5.indexBy(List.of("a","a"), x->x)); }
 @Test void lruReadRefreshesEntry(){ var c=new Practice5.LruCache<String,Integer>(2); c.put("a",1); c.put("b",2); c.get("a"); c.put("c",3); assertEquals(List.of("a","c"),c.keysOldestFirst()); }
 @Test void graphOrdersAndReportsCycle(){ var g=new Practice5.Graph<String>(); g.addVertex("A"); g.addVertex("B"); g.addDependency("B","A"); assertEquals(List.of("A","B"),g.plan()); g.addDependency("A","B"); assertTrue(assertThrows(IllegalStateException.class,g::plan).getMessage().contains("A")); }
 @Test void indexRemovesCourses(){ var index=new Practice5.TagIndex(); index.add(new Practice5.Course(1,"Java",Set.of("code","java"))); assertEquals(Set.of(1),index.intersection(Set.of("java"))); index.remove(1); assertTrue(index.intersection(Set.of("java")).isEmpty()); }
 @Test void repositorySnapshotsAreImmutable(){ var r=new Practice5.MemoryRepository<Integer,String>(); r.save(1,"one"); var snapshot=r.findAll(); r.save(2,"two"); assertEquals(List.of("one"),snapshot); assertThrows(UnsupportedOperationException.class,()->snapshot.add("x")); }
}
