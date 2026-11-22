package coursehub.practice9;

import java.time.Duration;
import java.util.*;
/** Запуск демонстрации лабораторной работы 9. */
public final class Practice9 {
    private Practice9() {}
    public static void main(String[] args) throws Exception {
        System.out.println("Потерянное обновление: " + RaceDemonstration.forcedLostUpdate() + " вместо 2");
        SessionRegistry registry = new SessionRegistry(); System.out.println("Сессия: " + registry.createIfAbsent("s1","Аня"));
        System.out.println("Очередь: " + QueueProcessor.processJobs(List.of(5,1,2,1),2,2,2,Duration.ofSeconds(5)));
        Enrollment enrollment = new LockedEnrollment(1); System.out.println("Зачисление: " + enrollment.enroll("Аня") + ", повтор: " + enrollment.enroll("Аня"));
    }
}
