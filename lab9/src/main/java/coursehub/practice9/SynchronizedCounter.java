package coursehub.practice9;

public final class SynchronizedCounter {
    private int value;
    // Монитор this защищает и чтение, и запись поля value.
    public synchronized void increment() { value++; }
    public synchronized int value() { return value; }
}
