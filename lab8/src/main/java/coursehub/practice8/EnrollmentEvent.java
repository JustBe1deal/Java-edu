package coursehub.practice8;

/** Событие состояния заявки на курс. */
public record EnrollmentEvent(String student, String courseCode, boolean completed) {
    public EnrollmentEvent {
        if (student == null || student.isBlank() || courseCode == null || courseCode.isBlank()) throw new IllegalArgumentException("Некорректное событие");
    }
}
