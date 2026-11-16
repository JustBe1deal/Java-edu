package coursehub.practice6;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.time.*;
import java.util.*;

class Practice6Test {
    private final Currency rub = Currency.getInstance("RUB");
    private Practice6.Money money(String s) { return new Practice6.Money(new BigDecimal(s), rub); }
    @Test void codeMatchesEntireInput() { assertEquals(101,Practice6.CourseCode.parse("JAVA-101").number()); assertThrows(IllegalArgumentException.class,()->Practice6.CourseCode.parse(" JAVA-101")); }
    @Test void leapDayIsStrict() { assertEquals(LocalDate.of(2024,2,29),Practice6.parseDate("date","29.02.2024")); assertThrows(Practice6.InvalidDateFieldException.class,()->Practice6.parseDate("date","29.02.2023")); }
    @Test void gapIsRejectedAndOverlapIsExplicit() {
        ZoneId zone=ZoneId.of("Europe/Berlin");
        assertThrows(Practice6.InvalidLocalTimeException.class,()->Practice6.resolve(LocalDateTime.of(2024,3,31,2,30),zone,false));
        var first=Practice6.resolve(LocalDateTime.of(2024,10,27,2,30),zone,false);
        var second=Practice6.resolve(LocalDateTime.of(2024,10,27,2,30),zone,true);
        assertEquals(Duration.ofHours(1),Duration.between(first.toInstant(),second.toInstant()));
    }
    @Test void allocationPreservesTotal() {
        var parts=money("10.00").allocate(3); assertEquals(List.of(money("3.34"),money("3.33"),money("3.33")),parts);
    }
    @Test void proportionalDiscountPreservesTotal() {
        var discounts=Practice6.allocateDiscount(List.of(money("10.00"),money("20.00"),money("30.00")),money("1.00"),RoundingMode.HALF_UP);
        assertEquals(money("1.00"),discounts.stream().reduce(money("0.00"),Practice6.Money::add));
    }
    @Test void currenciesCannotMix() { assertThrows(IllegalArgumentException.class,()->money("1.00").add(new Practice6.Money(new BigDecimal("1.00"),Currency.getInstance("USD")))); }
    @Test void discountDeadlineIsExclusive() {
        var deadline=Instant.parse("2024-01-01T00:00:00Z");
        assertEquals(money("90.00"),Practice6.price(money("100.00"),new BigDecimal("10"),deadline.minusNanos(1),deadline,RoundingMode.HALF_UP));
        assertEquals(money("100.00"),Practice6.price(money("100.00"),new BigDecimal("10"),deadline,deadline,RoundingMode.HALF_UP));
    }
    @Test void refundCountsOnlyFutureSessions() {
        Instant first=Instant.parse("2024-01-01T00:00:00Z");
        var plan=new Practice6.WebinarSeriesPlan(ZoneId.of("UTC"),List.of(first,first.plusSeconds(3600),first.plusSeconds(7200)));
        assertEquals(money("6.67"),Practice6.refund(new Practice6.Purchase(money("10.00"),plan),first,RoundingMode.HALF_UP));
    }
    @Test void sameSeedReproducesSample() { assertEquals(Practice6.randomSample(new Random(7),10,1,100),Practice6.randomSample(new Random(7),10,1,100)); }
    @Test void searchKeyAndDisplayDiffer() { assertEquals(Practice6.CourseTitle.of(" Java  Basics ").searchKey(),Practice6.CourseTitle.of("java basics").searchKey()); }
    @Test void weeklyScheduleKeepsLocalTimeAcrossDst() {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        var plan = Practice6.WebinarSeriesPlan.weekly(LocalDateTime.of(2024,3,24,10,0), zone, 2);
        assertEquals(LocalTime.of(10,0), plan.sessions().get(1).atZone(zone).toLocalTime());
        assertEquals(Duration.ofHours(167), Duration.between(plan.sessions().get(0),plan.sessions().get(1)));
    }
    @Test void refundBeforeAllSessionsKeepsExactAmount() {
        Instant first=Instant.parse("2024-02-29T12:00:00Z");
        var plan=new Practice6.WebinarSeriesPlan(ZoneId.of("UTC"),List.of(first));
        assertEquals(money("7.31"),Practice6.refund(new Practice6.Purchase(money("7.31"),plan),first.minusSeconds(1),RoundingMode.HALF_UP));
    }
}
