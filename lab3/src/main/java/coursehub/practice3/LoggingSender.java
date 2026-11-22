package coursehub.practice3;

import java.util.*;
import java.util.function.Consumer;
public final class LoggingSender implements NotificationSender {
    private final NotificationSender delegate; private final Consumer<String> log;
    public LoggingSender(NotificationSender delegate, Consumer<String> log) { this.delegate = Objects.requireNonNull(delegate); this.log = Objects.requireNonNull(log); }
    @Override public String send(String to, String message) { log.accept("before"); try { return delegate.send(to, message); } finally { log.accept("after"); } }
}
