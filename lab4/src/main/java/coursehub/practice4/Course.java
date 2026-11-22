package coursehub.practice4;

public final class Course {
    private final int id, duration; private int completed; private boolean open, hasSeats;
    public Course(int id, int duration, boolean open, boolean hasSeats) {
        if (id <= 0 || duration < EnrollmentRules.MIN_HOURS || duration > EnrollmentRules.MAX_HOURS) throw new IllegalArgumentException("Некорректная продолжительность или id");
        this.id = id; this.duration = duration; this.open = open; this.hasSeats = hasSeats;
    }
    public boolean isOpen() { return open; } public boolean hasSeats() { return hasSeats; }
    public int id() { return id; } public int duration() { return duration; } public int completed() { return completed; }
    public void completeHours(int hours) {
        if (hours <= 0 || hours > duration - completed) throw new IllegalArgumentException("Часы вне допустимого диапазона");
        completed += hours;
    }
}
