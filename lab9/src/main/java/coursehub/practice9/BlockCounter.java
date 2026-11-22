package coursehub.practice9;

public final class BlockCounter {
    private final Object lock = new Object(); private int value;
    public void increment() { synchronized (lock) { value++; } }
    public int value() { synchronized (lock) { return value; } }
}
