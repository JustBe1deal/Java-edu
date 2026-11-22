package coursehub.practice3;

public final class Greeting implements FirstGreeting, SecondGreeting {
    @Override public String greeting() { return FirstGreeting.super.greeting(); }
}
