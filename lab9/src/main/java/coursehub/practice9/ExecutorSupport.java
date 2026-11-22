package coursehub.practice9;

import java.util.concurrent.*;
/** Завершает пул потоков с ограниченным ожиданием. */
public final class ExecutorSupport {
    private ExecutorSupport() {}
    static void shutdown(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        try { if (!pool.awaitTermination(2,TimeUnit.SECONDS)) { pool.shutdownNow(); pool.awaitTermination(2,TimeUnit.SECONDS); } }
        catch (InterruptedException e) { pool.shutdownNow(); Thread.currentThread().interrupt(); throw e; }
    }
}
