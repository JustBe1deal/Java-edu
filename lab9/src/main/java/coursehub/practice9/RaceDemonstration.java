package coursehub.practice9;

import java.util.concurrent.*;
/** Воспроизводит потерянное обновление счётчика. */
public final class RaceDemonstration {
    private RaceDemonstration() {}
    /** Управляет порядком чтения и записи, чтобы воспроизвести гонку. */
    public static int forcedLostUpdate() throws InterruptedException, ExecutionException, TimeoutException {
        UnsafeCounter counter = new UnsafeCounter(); CyclicBarrier readBarrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> task = () -> { int old = counter.value; readBarrier.await(2,TimeUnit.SECONDS); counter.value = old + 1; return null; };
            Future<Void> a = executor.submit(task), b = executor.submit(task);
            a.get(3,TimeUnit.SECONDS); b.get(3,TimeUnit.SECONDS); return counter.value;
        } finally { ExecutorSupport.shutdown(executor); }
    }
}
