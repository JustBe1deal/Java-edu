package coursehub.practice9;

/** Намеренно небезопасный пример флага без гарантии видимости. */
public final class UnsafeWorker implements Runnable {
    private boolean running = true; // намеренно без volatile и без общей блокировки
    @Override public void run() { while (running && !Thread.currentThread().isInterrupted()) { Thread.onSpinWait(); } }
    public void stop() { running = false; }
    // Прерывание служит запасным способом остановки демонстрации, а не исправлением флага.
}
