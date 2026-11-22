package coursehub.practice3;

import java.util.*;
public abstract class AbstractNotificationSender implements NotificationSender {
    @Override public final String send(String recipient, String message) {
        if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("Пустой адресат");
        return doSend(recipient, Objects.requireNonNull(message));
    }
    protected abstract String doSend(String recipient, String message);
    public String describe(Object value) { return "object:" + value; }
    public String describe(String value) { return "string:" + value; }
}
