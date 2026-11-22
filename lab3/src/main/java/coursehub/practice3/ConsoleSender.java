package coursehub.practice3;

public final class ConsoleSender extends AbstractNotificationSender {
    @Override protected String doSend(String recipient, String message) { return "console " + recipient + ": " + message; }
}
