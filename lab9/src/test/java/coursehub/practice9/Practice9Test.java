package coursehub.practice9;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
class Practice9Test {
    @Test @Timeout(10) void forcedRaceAlwaysLosesUpdate() throws Exception { for(int i=0;i<20;i++) assertEquals(1,RaceDemonstration.forcedLostUpdate()); }
    @Test @Timeout(10) void synchronizedCountersDoNotLoseUpdates() throws Exception {
        var a=new SynchronizedCounter(); var b=new BlockCounter(); ExecutorService pool=Executors.newFixedThreadPool(4);
        try { var fs=new ArrayList<Future<?>>(); for(int i=0;i<4;i++) fs.add(pool.submit(()->{ for(int j=0;j<10000;j++){ a.increment(); b.increment(); } }));
            for(var f:fs) f.get(5,TimeUnit.SECONDS); assertEquals(40000,a.value()); assertEquals(40000,b.value());
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS)); }
    }
    @Test @Timeout(5) void volatileWorkerStops() throws Exception {
        Worker worker=new Worker(); Thread thread=new Thread(worker); thread.start();
        try { assertTrue(worker.awaitStarted(Duration.ofSeconds(1))); worker.stop(); thread.join(2000); assertFalse(thread.isAlive()); }
        finally { worker.stop(); thread.interrupt(); thread.join(1000); }
    }
    @Test @Timeout(5) void sessionCreationIsAtomic() throws Exception {
        SessionRegistry registry=new SessionRegistry(); ExecutorService pool=Executors.newFixedThreadPool(8); CountDownLatch start=new CountDownLatch(1); AtomicInteger success=new AtomicInteger();
        try { var tasks=new ArrayList<Future<?>>(); for(int i=0;i<8;i++) tasks.add(pool.submit(()->{ start.await(); if(registry.createIfAbsent("same","user")) success.incrementAndGet(); return null; }));
            start.countDown(); for(var f:tasks) f.get(2,TimeUnit.SECONDS); assertEquals(1,success.get());
        } finally { start.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS)); }
    }
    @Test @Timeout(8) void boundedQueueProcessesEveryJobOnce() throws Exception {
        assertEquals(List.of(2,2,4,10),QueueProcessor.processJobs(List.of(5,1,2,1),2,3,1,Duration.ofSeconds(3)));
        assertEquals(List.of(),QueueProcessor.processJobs(List.of(),2,2,1,Duration.ofSeconds(3)));
    }
    @Test @Timeout(10) void bothEnrollmentImplementationsPreserveInvariants() throws Exception {
        for (Enrollment enrollment : List.of(new LockedEnrollment(4),new ConcurrentEnrollment(4))) {
            ExecutorService pool=Executors.newFixedThreadPool(16); CountDownLatch ready=new CountDownLatch(16), start=new CountDownLatch(1); AtomicInteger success=new AtomicInteger();
            try { var tasks=new ArrayList<Future<?>>(); for(int i=0;i<16;i++){ final String id="s"+(i%8); tasks.add(pool.submit(()->{ ready.countDown(); start.await(); if(enrollment.enroll(id)) success.incrementAndGet(); return null; })); }
                assertTrue(ready.await(2,TimeUnit.SECONDS)); start.countDown(); for(var f:tasks) f.get(3,TimeUnit.SECONDS);
                assertEquals(4,success.get()); assertEquals(4,enrollment.students().size());
                var before=enrollment.students(); assertFalse(enrollment.enroll(before.iterator().next())); assertFalse(enrollment.enroll("new")); assertEquals(before,enrollment.students());
            } finally { start.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS)); }
        }
    }
    @Test void zeroCapacityRejectsWithoutChange() { for(Enrollment e:List.of(new LockedEnrollment(0),new ConcurrentEnrollment(0))) { assertFalse(e.enroll("s")); assertTrue(e.students().isEmpty()); } }
}
