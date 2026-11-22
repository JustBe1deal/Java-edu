package coursehub.practice9;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
/** Обрабатывает заявки через ограниченную очередь и пул потоков. */
public final class QueueProcessor {
    private QueueProcessor() {}
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
            ExecutorSupport.shutdown(pool);
        }
    }
    private static long remaining(long deadline) throws TimeoutException {
        long time = deadline - System.nanoTime(); if (time <= 0) throw new TimeoutException("Превышено время ожидания"); return time;
    }
}
