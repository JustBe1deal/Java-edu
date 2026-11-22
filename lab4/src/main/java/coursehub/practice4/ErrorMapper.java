package coursehub.practice4;

/** Преобразует известные исключения в стабильные коды ошибок. */
public final class ErrorMapper {
    private ErrorMapper() {}
    public static PublicError publicError(Exception error) {
        if (error instanceof CourseImportException) return new PublicError(Reason.INVALID_DATA.name());
        if (error instanceof InfrastructureException) return new PublicError(Reason.IO_ERROR.name());
        if (error instanceof EnrollmentRejectedException e) return new PublicError(e.reason.name());
        throw new IllegalArgumentException("Неизвестная ошибка", error);
    }
}
