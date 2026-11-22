package coursehub.practice9;

public final class UnsafeCounter {
    int value; // пакетный доступ только для демонстрации гонки
    public void increment() { value++; }
    public int value() { return value; }
}
