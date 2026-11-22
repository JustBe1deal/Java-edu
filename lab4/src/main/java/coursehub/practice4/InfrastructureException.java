package coursehub.practice4;

public final class InfrastructureException extends Exception {
    public InfrastructureException(Throwable cause) { super("Ошибка чтения", cause); }
}
