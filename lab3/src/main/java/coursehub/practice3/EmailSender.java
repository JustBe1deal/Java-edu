package coursehub.practice3;

public final class EmailSender extends AbstractNotificationSender {
    @Override protected String doSend(String recipient, String message) { return "email " + recipient + ": " + message; }
    @Override public String describe(Object value) { return "email-object:" + value; }
}
