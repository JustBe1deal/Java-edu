package coursehub.practice9;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public final class Worker implements Runnable {
    private volatile boolean running = true;
    private final CountDownLatch started = new CountDownLatch(1);
    private final AtomicLong iterations = new AtomicLong();
    @Override public void run() { started.countDown(); while (running && !Thread.currentThread().isInterrupted()) iterations.incrementAndGet(); }
    public boolean awaitStarted(Duration timeout) throws InterruptedException { return started.await(timeout.toMillis(),TimeUnit.MILLISECONDS); }
    public void stop() { running = false; }
    public long iterations() { return iterations.get(); }
}
