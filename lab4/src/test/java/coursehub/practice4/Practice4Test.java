package coursehub.practice4;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.io.*;
import java.util.*;

class Practice4Test {
    @Test void completeHoursAddsProgress() {
        // Arrange: каждый тест создаёт независимый объект.
        var course = new Course(1, 40, true, true);
        // Act: только проверяемое действие.
        course.completeHours(10);
        // Assert: одна проверка результата.
        assertEquals(10, course.completed());
    }
    @ParameterizedTest(name="duration {0}: valid={1}")
    @CsvSource({"0,false", "1,true", "1000,true", "1001,false"})
    void durationBoundaries(int n, boolean valid) {
        if (valid) assertEquals(n, new Course(1,n,true,true).duration());
        else assertThrows(IllegalArgumentException.class, () -> new Course(1,n,true,true));
    }
    @ParameterizedTest(name="code {0} -> {1}")
    @CsvSource({"' java-101 ',JAVA-101", "ab-001,AB-001", "JAVA-999,JAVA-999"})
    void codeNormalization(String input, String expected) { assertEquals(expected, new CourseCode(input).value()); }
    @ParameterizedTest(name="invalid code: {0}")
    @ValueSource(strings={"JAVA-1", "JAVA-1000", "ЯВА-101", "J-101", "JAVA 101"})
    void invalidCodes(String input) { assertThrows(IllegalArgumentException.class, () -> new CourseCode(input)); }
    @Test void rejectionKeepsProgressAndMessage() {
        var c = new Course(1, 10, true, true); c.completeHours(3);
        var ex = assertThrows(IllegalArgumentException.class, () -> c.completeHours(8));
        assertEquals("Часы вне допустимого диапазона", ex.getMessage()); assertEquals(3, c.completed());
    }
    @ParameterizedTest(name="open={0}, seats={1}, prerequisite={2}, duplicate={3} => {4}")
    @CsvSource({"false,false,false,true,CLOSED", "true,true,true,true,DUPLICATE", "true,true,false,false,PREREQUISITE", "true,false,true,false,NO_SEATS", "true,true,true,false,ACCEPT"})
    void decisionTable(boolean open, boolean seats, boolean prerequisite, boolean duplicate, String expected) {
        var result = EnrollmentRules.enroll(new Course(1,10,open,seats), prerequisite, duplicate);
        assertEquals(expected, result.accepted() ? "ACCEPT" : result.reason().name());
    }
    @Test void parserPreservesNumberCauseAndRecord() {
        var e = assertThrows(CourseImportException.class, () -> CourseImporter.parseCourse("x|20", 7));
        assertEquals(7, e.record); assertInstanceOf(NumberFormatException.class, e.getCause());
    }
    @Test void parseMalformedFields() { assertThrows(CourseImportException.class, () -> CourseImporter.parseCourse("1", 1)); }
    @Test void parseInvalidDomain() { assertThrows(CourseImportException.class, () -> CourseImporter.parseCourse("1|0", 1)); }
    @Test void findMissing() { assertTrue(EnrollmentRules.findCourse(List.of(), 1).isEmpty()); }
    @Test void findPresent() { var c = new Course(1,10,true,true); assertSame(c, EnrollmentRules.findCourse(List.of(c),1).orElseThrow()); }
    @Test void closeOnSuccess() throws Exception { var log = new ArrayList<String>(); ResourceDemo.traced(log,false,false); assertEquals(List.of("open","use","close"),log); }
    @Test void closeOnUseFailure() { var log = new ArrayList<String>(); assertThrows(IOException.class, () -> ResourceDemo.traced(log,true,false)); assertEquals(List.of("open","use","close"),log); }
    @Test void suppressedCloseFailure() {
        var log = new ArrayList<String>(); var e = assertThrows(IOException.class, () -> ResourceDemo.traced(log,true,true));
        assertEquals("use failed", e.getMessage()); assertEquals("close failed",e.getSuppressed()[0].getMessage());
    }
    static class FailingCloseReader extends StringReader {
        int lines;
        FailingCloseReader(String s) { super(s); }
        @Override public int read(char[] b, int off, int len) throws IOException { int n=super.read(b,off,len); if(n>0) for(int i=off;i<off+n;i++) if(b[i]=='\n') lines++; return n; }
        @Override public void close() { super.close(); throw new UncheckedIOException(new IOException("close failed")); }
    }
    @Test void importFailureDoesNotPublishAndSuppressesClose() {
        Reader r = new Reader() {
            private final StringReader inner = new StringReader("1|10\nbad|20\n3|30\n");
            @Override public int read(char[] b,int off,int len) throws IOException { return inner.read(b,off,len); }
            @Override public void close() throws IOException { throw new IOException("close failed"); }
        };
        var e = assertThrows(CourseImportException.class, () -> CourseImporter.importCourses(r));
        assertEquals(2,e.record); assertEquals("close failed",e.getSuppressed()[0].getMessage());
    }
    @Test void importSuccess() throws Exception { assertEquals(2, CourseImporter.importCourses(new StringReader("1|10\n2|20\n")).size()); }
    @Test void importCloseFailureIsInfrastructureError() {
        Reader r = new Reader() { public int read(char[] b,int o,int l) { return -1; } public void close() throws IOException { throw new IOException("close"); } };
        var e = assertThrows(InfrastructureException.class, () -> CourseImporter.importCourses(r)); assertEquals("close",e.getCause().getMessage());
    }
    @Test void publicCodesAreStable() { assertEquals("INVALID_DATA", ErrorMapper.publicError(new CourseImportException(3,new NumberFormatException())).code()); }
    @Test void programmingErrorIsNotConverted() { assertThrows(IllegalArgumentException.class, () -> ErrorMapper.publicError(new IllegalStateException())); }
    @Test void finallyRunsOnReturn() { var log = new ArrayList<String>(); assertEquals("try result",ExceptionExamples.finallyExample(false,log)); assertEquals(List.of("try","finally"),log); }
    @Test void finallyRunsOnCatch() { var log = new ArrayList<String>(); assertEquals("catch result",ExceptionExamples.finallyExample(true,log)); assertEquals(List.of("try","catch","finally"),log); }
    @Test void rejectionDoesNotExposeIdsInMessage() { var e = new EnrollmentRejectedException(123,456,Reason.CLOSED,new IOException()); assertFalse(e.toString().contains("123")); assertEquals(456,e.courseId); assertNotNull(e.getCause()); }
    @Test void useCaseAcceptsAfterFullImport() { assertEquals("ACCEPTED", EnrollmentUseCase.importAndEnroll(new StringReader("1|10\n"),1,true,false).code()); }
    @Test void useCaseRejectsExpectedBusinessOutcome() { assertEquals("PREREQUISITE", EnrollmentUseCase.importAndEnroll(new StringReader("1|10\n"),1,false,false).code()); }
    @Test void useCaseDoesNotExposePartiallyImportedCourse() {
        var result = EnrollmentUseCase.importAndEnroll(new StringReader("1|10\nbad|10\n"),1,true,false);
        assertEquals("INVALID_DATA",result.code()); assertNull(result.course()); assertInstanceOf(NumberFormatException.class, result.diagnostic().getCause());
    }
    @Test void useCaseReportsMissingCourse() { assertEquals("NOT_FOUND",EnrollmentUseCase.importAndEnroll(new StringReader("1|10\n"),2,true,false).code()); }
    @Test void useCaseLetsProgrammingErrorEscape() { assertThrows(IllegalArgumentException.class,()->EnrollmentUseCase.importAndEnroll(new StringReader(""),0,true,false)); }
}
