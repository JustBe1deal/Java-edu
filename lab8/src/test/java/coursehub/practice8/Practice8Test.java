package coursehub.practice8;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.*;
import static coursehub.practice8.Practice8.*;
class Practice8Test {
    private final Course a = new Course("A",Level.BEGINNER,10,List.of(" Java ","streams"));
    private final Course b = new Course("B",Level.ADVANCED,20,List.of("JAVA"," Threads "));
    @Test void sortingDoesNotDependOnInputOrder() { assertEquals(sortedCourses(List.of(a,b)),sortedCourses(List.of(b,a))); }
    @Test void tieBreakerIncludesModules() {
        var x=new Course("A",Level.BEGINNER,10,List.of("a\u0000b")); var y=new Course("A",Level.BEGINNER,10,List.of("a","b"));
        assertEquals(sortedCourses(List.of(x,y)),sortedCourses(List.of(y,x)));
    }
    @Test void topicsAndIndexNormalizeAndDeduplicate() {
        assertEquals(Set.of("java","streams","threads"),topics(List.of(a,b)));
        assertEquals(Set.of("A","B"),topicIndex(List.of(a,b)).get("java"));
        assertThrows(UnsupportedOperationException.class,()->topicIndex(List.of(a)).get("java").add("X"));
    }
    @Test void reduceUsesNeutralIdentity() { assertEquals(30,totalDuration(List.of(a,b))); assertEquals(40,wrongIdentity(List.of(a,b))); assertEquals(new Totals(2,30),totals(List.of(a,b))); }
    @Test void groupingAndOptional() { assertEquals(new LevelSummary(1,10,10.0),byLevel(List.of(a,b)).get(Level.BEGINNER)); assertEquals("Курсов нет",longestLabel(List.of())); }
    @Test void customCollectorWorksWithParallelAndEmptyStreams() {
        var list=List.of(a,b,a,b); assertEquals(list.stream().collect(statsCollector()),list.parallelStream().collect(statsCollector()));
        assertTrue(Stream.<Course>empty().collect(statsCollector()).min().isEmpty());
        assertEquals(new Stats(4,60,OptionalInt.of(10),OptionalInt.of(20)),list.stream().collect(statsCollector()));
    }
    @Test void reportIsDeterministicAndCountsDuplicates() {
        var events=List.of(new EnrollmentEvent("s","A",false),new EnrollmentEvent("s","A",true),new EnrollmentEvent("t","B",false),new EnrollmentEvent("s","X",true));
        var reversed=new ArrayList<>(events); Collections.reverse(reversed);
        var r=report(events.stream(),List.of(a,b),1); assertEquals(r,report(reversed.stream(),List.of(b,a),1));
        assertEquals(2,r.uniqueStudents()); assertEquals(List.of("s","t"),r.studentIds()); assertEquals(2,r.anomalyCount()); assertEquals(1,r.anomalies().size());
        assertEquals(1,r.courses().get(0).completed()); assertEquals(1.0,r.courses().get(0).completionRate());
    }
    @Test void reportRejectsParallelAndDuplicateCatalog() {
        assertThrows(IllegalArgumentException.class,()->report(Stream.<EnrollmentEvent>empty().parallel(),List.of(a),1));
        assertThrows(IllegalArgumentException.class,()->report(Stream.empty(),List.of(a,a),1));
    }
}
