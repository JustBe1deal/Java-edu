package coursehub.practice3;

/** Закрытый набор допустимых результатов зачисления. */
public sealed interface EnrollmentResult permits Accepted, Rejected, WaitListed {}
