package coursehub.practice8;

public record CourseResult(String code, Level level, long enrolled, long completed) {
    public double completionRate() { return enrolled == 0 ? 0.0 : (double) completed / enrolled; }
}
