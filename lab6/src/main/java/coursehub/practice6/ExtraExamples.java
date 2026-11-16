package coursehub.practice6;
import java.math.*;
import java.time.*;
import java.util.*;
/** Небольшие запускаемые эксперименты к заданиям 1, 2, 4, 5, 7–12. */
public final class ExtraExamples {
    private ExtraExamples() {}
    public static void main(String[] args) {
        String literal="Java", fromNew=new String("Java"), compile="Ja"+"va", prefix="Ja", runtime=prefix+"va";
        System.out.println("identity: " + (literal==compile) + ", " + (literal==fromNew) + ", " + (literal==runtime) + ", intern=" + (literal==runtime.intern()));
        System.out.println("equals: " + literal.equals(fromNew));
        long start=System.nanoTime(); String slow=Practice6.reportPlus(10000); long plus=System.nanoTime()-start;
        start=System.nanoTime(); String fast=Practice6.reportBuilder(10000); long builder=System.nanoTime()-start;
        System.out.println("Отчёты равны: "+slow.equals(fast)+"; +="+plus+" ns; builder="+builder+" ns");
        Integer absent=null;
        try { int n=absent; System.out.println(n); } catch (NullPointerException e) { System.out.println("Unboxing null: "+e.getClass().getSimpleName()); }
        Integer smallA=Integer.valueOf(100), smallB=Integer.valueOf(100), bigA=Integer.valueOf(1000), bigB=Integer.valueOf(1000);
        System.out.println("Integer ==: "+(smallA==smallB)+", "+(bigA==bigB)+"; equals: "+bigA.equals(bigB));
        System.out.println("double constructor: "+new BigDecimal(0.1));
        System.out.println("valueOf: "+BigDecimal.valueOf(0.1)+"; string: "+new BigDecimal("0.1"));
        System.out.println("Три цены: "+new BigDecimal("0.10").add(new BigDecimal("0.20")).add(new BigDecimal("0.30")));
        System.out.println("Выборка: "+Practice6.randomSample(new Random(42),5,0,100));
        System.out.println("Math: min="+Math.min(1.2,2.3)+", max="+Math.max(1.2,2.3)+", round="+Math.round(1.5));
        LocalDate startDate=LocalDate.of(2024,2,29);
        System.out.println("Модули: "+startDate+", "+startDate.plusWeeks(1)+", "+startDate.plusWeeks(2)+"; последний день="+startDate.plusDays(20)+"; leap="+startDate.isLeapYear());
        ZoneId berlin=ZoneId.of("Europe/Berlin"); ZonedDateTime before=ZonedDateTime.of(2024,3,30,12,0,0,0,berlin);
        System.out.println("Period: "+before.plus(Period.ofDays(1))+"; Duration: "+before.plus(Duration.ofHours(24)));
        Instant instant=Instant.parse("2024-02-29T12:00:00Z");
        for(String zone:List.of("UTC","Asia/Novosibirsk","America/New_York")) System.out.println(zone+": "+instant.atZone(ZoneId.of(zone)));
        ZonedDateTime spring=ZonedDateTime.of(2024,3,31,1,30,0,0,berlin);
        System.out.println("DST: "+spring+" + hour="+spring.plusHours(1)+" + day="+spring.plusDays(1));
        System.out.println("Автоматический gap: "+ZonedDateTime.of(LocalDateTime.of(2024,3,31,2,30),berlin));
    }
}
