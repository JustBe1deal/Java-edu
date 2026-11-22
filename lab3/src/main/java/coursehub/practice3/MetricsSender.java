package coursehub.practice3;

import java.util.*;
public final class MetricsSender implements NotificationSender {
    private final NotificationSender delegate; public int attempts; public int failures;
    public MetricsSender(NotificationSender delegate) { this.delegate = Objects.requireNonNull(delegate); }
    @Override public String send(String to, String message) { attempts++; try { return delegate.send(to, message); } catch (RuntimeException e) { failures++; throw e; } }
}
