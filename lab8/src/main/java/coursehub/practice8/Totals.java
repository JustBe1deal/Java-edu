package coursehub.practice8;

public record Totals(long courses, long duration) {
    public Totals combine(Totals other) { return new Totals(Math.addExact(courses,other.courses), Math.addExact(duration,other.duration)); }
}
