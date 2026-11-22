package coursehub.practice6;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.time.*;
import java.util.*;

class Practice6Test {
    private final Currency rub = Currency.getInstance("RUB");
    private Money money(String s) { return new Money(new BigDecimal(s), rub); }
    @Test void codeMatchesEntireInput() { assertEquals(101,CourseCode.parse("JAVA-101").number()); assertThrows(IllegalArgumentException.class,()->CourseCode.parse(" JAVA-101")); }
    @Test void leapDayIsStrict() { assertEquals(LocalDate.of(2024,2,29),DateParser.parseDate("date","29.02.2024")); assertThrows(InvalidDateFieldException.class,()->DateParser.parseDate("date","29.02.2023")); }
    @Test void gapIsRejectedAndOverlapIsExplicit() {
        ZoneId zone=ZoneId.of("Europe/Berlin");
        assertThrows(InvalidLocalTimeException.class,()->LocalDateTimeResolver.resolve(LocalDateTime.of(2024,3,31,2,30),zone,false));
        var first=LocalDateTimeResolver.resolve(LocalDateTime.of(2024,10,27,2,30),zone,false);
        var second=LocalDateTimeResolver.resolve(LocalDateTime.of(2024,10,27,2,30),zone,true);
        assertEquals(Duration.ofHours(1),Duration.between(first.toInstant(),second.toInstant()));
    }
    @Test void allocationPreservesTotal() {
        var parts=money("10.00").allocate(3); assertEquals(List.of(money("3.34"),money("3.33"),money("3.33")),parts);
    }
    @Test void proportionalDiscountPreservesTotal() {
        var discounts=DiscountAllocator.allocateDiscount(List.of(money("10.00"),money("20.00"),money("30.00")),money("1.00"),RoundingMode.HALF_UP);
        assertEquals(money("1.00"),discounts.stream().reduce(money("0.00"),Money::add));
    }
    @Test void currenciesCannotMix() { assertThrows(IllegalArgumentException.class,()->money("1.00").add(new Money(new BigDecimal("1.00"),Currency.getInstance("USD")))); }
    @Test void discountDeadlineIsExclusive() {
        var deadline=Instant.parse("2024-01-01T00:00:00Z");
        assertEquals(money("90.00"),PricingService.price(money("100.00"),new BigDecimal("10"),deadline.minusNanos(1),deadline,RoundingMode.HALF_UP));
        assertEquals(money("100.00"),PricingService.price(money("100.00"),new BigDecimal("10"),deadline,deadline,RoundingMode.HALF_UP));
    }
    @Test void refundCountsOnlyFutureSessions() {
        Instant first=Instant.parse("2024-01-01T00:00:00Z");
        var plan=new WebinarSeriesPlan(ZoneId.of("UTC"),List.of(first,first.plusSeconds(3600),first.plusSeconds(7200)));
        assertEquals(money("6.67"),RefundService.refund(new Purchase(money("10.00"),plan),first,RoundingMode.HALF_UP));
    }
    @Test void sameSeedReproducesSample() { assertEquals(RandomSampleService.randomSample(new Random(7),10,1,100),RandomSampleService.randomSample(new Random(7),10,1,100)); }
    @Test void searchKeyAndDisplayDiffer() { assertEquals(CourseTitle.of(" Java  Basics ").searchKey(),CourseTitle.of("java basics").searchKey()); }
    @Test void weeklyScheduleKeepsLocalTimeAcrossDst() {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        var plan = WebinarSeriesPlan.weekly(LocalDateTime.of(2024,3,24,10,0), zone, 2);
        assertEquals(LocalTime.of(10,0), plan.sessions().get(1).atZone(zone).toLocalTime());
        assertEquals(Duration.ofHours(167), Duration.between(plan.sessions().get(0),plan.sessions().get(1)));
    }
    @Test void refundBeforeAllSessionsKeepsExactAmount() {
        Instant first=Instant.parse("2024-02-29T12:00:00Z");
        var plan=new WebinarSeriesPlan(ZoneId.of("UTC"),List.of(first));
        assertEquals(money("7.31"),RefundService.refund(new Purchase(money("7.31"),plan),first.minusSeconds(1),RoundingMode.HALF_UP));
    }
}
