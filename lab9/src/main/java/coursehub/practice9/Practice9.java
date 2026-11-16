package coursehub.practice9;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Примеры конкурентного доступа и безопасных контрактов. */
public final class Practice9 {
    private Practice9() {}
    public static final class UnsafeCounter {
        private int value;
        public void increment() { value++; }
        public int value() { return value; }
    }
    public static final class SynchronizedCounter {
        private int value;
        // Монитор this защищает и чтение, и запись поля value.
        public synchronized void increment() { value++; }
        public synchronized int value() { return value; }
    }
    public static final class BlockCounter {
        private final Object lock = new Object(); private int value;
        public void increment() { synchronized (lock) { value++; } }
        public int value() { synchronized (lock) { return value; } }
    }
    /** Учебная детерминированная гонка: оба потока читают одно старое значение. */
    public static int forcedLostUpdate() throws InterruptedException, ExecutionException, TimeoutException {
        UnsafeCounter counter = new UnsafeCounter(); CyclicBarrier readBarrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> task = () -> { int old = counter.value; readBarrier.await(2,TimeUnit.SECONDS); counter.value = old + 1; return null; };
            Future<Void> a = executor.submit(task), b = executor.submit(task);
            a.get(3,TimeUnit.SECONDS); b.get(3,TimeUnit.SECONDS); return counter.value;
        } finally { shutdown(executor); }
    }
    public static final class Worker implements Runnable {
        private volatile boolean running = true;
        private final CountDownLatch started = new CountDownLatch(1);
        private final AtomicLong iterations = new AtomicLong();
        @Override public void run() { started.countDown(); while (running && !Thread.currentThread().isInterrupted()) iterations.incrementAndGet(); }
        public boolean awaitStarted(Duration timeout) throws InterruptedException { return started.await(timeout.toMillis(),TimeUnit.MILLISECONDS); }
        public void stop() { running = false; }
        public long iterations() { return iterations.get(); }
    }
    /** Некорректный флаг показан только для анализа: нет happens-before между потоками. */
    public static final class UnsafeWorker implements Runnable {
        private boolean running = true; // намеренно без volatile и без общей блокировки
        @Override public void run() { while (running && !Thread.currentThread().isInterrupted()) { Thread.onSpinWait(); } }
        public void stop() { running = false; }
        // Прерывание служит запасным способом остановки демонстрации, а не исправлением флага.
    }
    public static final class SessionRegistry {
        private final ConcurrentMap<String,String> sessions = new ConcurrentHashMap<>();
        public boolean createIfAbsent(String id, String user) {
            return sessions.putIfAbsent(Objects.requireNonNull(id),Objects.requireNonNull(user)) == null;
        }
        public Optional<String> find(String id) { return Optional.ofNullable(sessions.get(Objects.requireNonNull(id))); }
        public boolean remove(String id, String user) { return sessions.remove(Objects.requireNonNull(id),Objects.requireNonNull(user)); }
    }
    public interface Enrollment {
        boolean enroll(String student);
        Set<String> students();
    }
    /** Одна блокировка защищает общую проверку, добавление и снимок. */
    public static final class LockedEnrollment implements Enrollment {
        private final int capacity; private final Set<String> students = new HashSet<>();
        public LockedEnrollment(int capacity) { if (capacity < 0) throw new IllegalArgumentException("capacity < 0"); this.capacity = capacity; }
        @Override public synchronized boolean enroll(String student) {
            Objects.requireNonNull(student);
            if (students.contains(student) || students.size() >= capacity) return false;
            return students.add(student);
        }
        @Override public synchronized Set<String> students() { return Set.copyOf(students); }
    }
    /** CAS меняет весь неизменяемый снимок атомарно: отказ не публикует изменения. */
    public static final class ConcurrentEnrollment implements Enrollment {
        private final int capacity;
        private final AtomicReference<Set<String>> state = new AtomicReference<>(Set.of());
        public ConcurrentEnrollment(int capacity) { if (capacity < 0) throw new IllegalArgumentException("capacity < 0"); this.capacity = capacity; }
        @Override public boolean enroll(String student) {
            Objects.requireNonNull(student);
            while (true) {
                Set<String> before = state.get();
                if (before.contains(student) || before.size() >= capacity) return false;
                Set<String> copy = new HashSet<>(before); copy.add(student);
                if (state.compareAndSet(before, Set.copyOf(copy))) return true;
                // Другой поток успел изменить снимок: повторяем проверку на новом.
            }
        }
        @Override public Set<String> students() { return state.get(); }
    }
    private record Job(int value, boolean poison) {}
    private static ThreadFactory namedFactory() {
        AtomicInteger number = new AtomicInteger();
        return task -> new Thread(task,"coursehub-queue-" + number.incrementAndGet());
    }
    /** Ограниченный pipeline: обработка удваивает каждое значение, результат отсортирован и сохраняет повторы. */
    public static List<Integer> processJobs(List<Integer> jobs, int producerCount, int consumerCount, int queueCapacity, Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        jobs = List.copyOf(jobs);
        if (producerCount <= 0 || consumerCount <= 0 || queueCapacity <= 0 || timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("Некорректные параметры");
        final List<Integer> input = jobs; BlockingQueue<Job> queue = new ArrayBlockingQueue<>(queueCapacity);
        ExecutorService pool = Executors.newFixedThreadPool(producerCount + consumerCount,namedFactory());
        List<Future<?>> tasks = new ArrayList<>(); List<Future<List<Integer>>> consumers = new ArrayList<>();
        long deadline = System.nanoTime() + timeout.toNanos(); boolean finished = false;
        try {
            for (int i=0;i<consumerCount;i++) {
                Future<List<Integer>> f = pool.submit(() -> {
                    List<Integer> result = new ArrayList<>();
                    while (true) { Job job = queue.take(); if (job.poison()) return result; result.add(job.value() * 2); }
                });
                consumers.add(f); tasks.add(f);
            }
            List<Future<?>> producers = new ArrayList<>();
            for (int i=0;i<producerCount;i++) {
                final int offset = i;
                Future<?> f = pool.submit(() -> { for (int j=offset;j<input.size();j+=producerCount) queue.put(new Job(input.get(j),false)); return null; });
                producers.add(f); tasks.add(f);
            }
            for (Future<?> producer : producers) producer.get(remaining(deadline),TimeUnit.NANOSECONDS);
            for (int i=0;i<consumerCount;i++) if (!queue.offer(new Job(0,true),remaining(deadline),TimeUnit.NANOSECONDS)) throw new TimeoutException("Не удалось завершить обработку");
            List<Integer> output = new ArrayList<>();
            for (Future<List<Integer>> consumer : consumers) output.addAll(consumer.get(remaining(deadline),TimeUnit.NANOSECONDS));
            Collections.sort(output); finished = true; return List.copyOf(output);
        } finally {
            if (!finished) for (Future<?> task : tasks) task.cancel(true);
            shutdown(pool);
        }
    }
    private static long remaining(long deadline) throws TimeoutException {
        long time = deadline - System.nanoTime(); if (time <= 0) throw new TimeoutException("Превышено время ожидания"); return time;
    }
    private static void shutdown(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        try { if (!pool.awaitTermination(2,TimeUnit.SECONDS)) { pool.shutdownNow(); pool.awaitTermination(2,TimeUnit.SECONDS); } }
        catch (InterruptedException e) { pool.shutdownNow(); Thread.currentThread().interrupt(); throw e; }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Потерянное обновление: " + forcedLostUpdate() + " вместо 2");
        SessionRegistry registry = new SessionRegistry(); System.out.println("Сессия: " + registry.createIfAbsent("s1","Аня"));
        System.out.println("Очередь: " + processJobs(List.of(5,1,2,1),2,2,2,Duration.ofSeconds(5)));
        Enrollment enrollment = new LockedEnrollment(1); System.out.println("Зачисление: " + enrollment.enroll("Аня") + ", повтор: " + enrollment.enroll("Аня"));
    }
}
