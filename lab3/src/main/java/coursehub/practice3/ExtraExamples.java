package coursehub.practice3;
import java.util.*;

/** Небольшие независимые примеры для заданий о переопределении и равенстве. */
public final class ExtraExamples {
    private ExtraExamples() {}
    public static class Parent {
        public String show(Object value) { return "Parent(Object)"; }
    }
    public static final class AccidentalOverload extends Parent {
        // Это НЕ override: отличается тип аргумента. Без @Override компилятор допускает перегрузку.
        public String show(String value) { return "Child(String)"; }
    }
    public static class ReturnParent {
        public Number amount(Object value) { return 1; }
        public void send(String value) { System.out.println(value); }
    }
    public static final class CorrectMethods extends ReturnParent {
        // public сохранён, Integer — допустимый подтип результата Number.
        @Override public Integer amount(Object value) { return 2; }
        @Override public void send(String value) { System.out.println(value); }
    }
    public static final class CorrectOverride extends Parent {
        @Override public String show(Object value) { return "Child(Object)"; }
    }
    public static class BadPoint {
        final int x, y; BadPoint(int x,int y) { this.x=x; this.y=y; }
        @Override public boolean equals(Object obj) { return obj instanceof BadPoint p && x==p.x && y==p.y; }
        @Override public int hashCode() { return Objects.hash(x,y); }
    }
    public static final class BadColoredPoint extends BadPoint {
        final String color; BadColoredPoint(int x,int y,String color) { super(x,y); this.color=color; }
        @Override public boolean equals(Object obj) { return obj instanceof BadColoredPoint p && super.equals(p) && color.equals(p.color); }
        @Override public int hashCode() { return Objects.hash(super.hashCode(),color); }
    }
    public static class StrictPoint {
        final int x,y; StrictPoint(int x,int y) { this.x=x; this.y=y; }
        @Override public boolean equals(Object o) { if (o==null || getClass()!=o.getClass()) return false; StrictPoint p=(StrictPoint)o; return x==p.x && y==p.y; }
        @Override public int hashCode() { return Objects.hash(x,y); }
    }
    public record Point(int x,int y) {}
    public record ColoredPoint(Point point,String color) {}
    public static void main(String[] args) {
        Parent accidental = new AccidentalOverload();
        System.out.println(accidental.show("x")); // Parent(Object), тип ссылки Parent
        Parent corrected = new CorrectOverride();
        System.out.println(corrected.show("x")); // Child(Object), override
        BadPoint plain = new BadPoint(1,2), colored = new BadColoredPoint(1,2,"red");
        System.out.println("Нарушение симметрии: " + plain.equals(colored) + " / " + colored.equals(plain));
        System.out.println(new ColoredPoint(new Point(1,2),"red"));
    }
}
