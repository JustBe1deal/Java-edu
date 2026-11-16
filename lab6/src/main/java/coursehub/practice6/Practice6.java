package coursehub.practice6;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.zone.ZoneRules;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.regex.*;

/** Строки, денежная арифметика и время для CourseHub. */
public final class Practice6 {
    private Practice6() {}
    public record CourseCode(String prefix, int number) {
        private static final Pattern PATTERN = Pattern.compile("([A-Z]{2,10})-([0-9]{3})");
        public static CourseCode parse(String text) {
            Matcher m = PATTERN.matcher(Objects.requireNonNull(text));
            if (!m.matches()) throw new IllegalArgumentException("Код: только ASCII A-Z и три цифры");
            return new CourseCode(m.group(1), Integer.parseInt(m.group(2)));
        }
    }
    public static String reportBuilder(int lines) {
        if (lines < 0) throw new IllegalArgumentException("Отрицательное количество");
        StringBuilder b = new StringBuilder(); for (int i = 0; i < lines; i++) b.append("Строка ").append(i).append('\n'); return b.toString();
    }
    public static String reportPlus(int lines) {
        if (lines < 0) throw new IllegalArgumentException("Отрицательное количество");
        String report = ""; for (int i = 0; i < lines; i++) report += "Строка " + i + '\n'; return report;
    }
    public static int requireInteger(Integer value) { return Objects.requireNonNull(value, "Число обязательно"); }
    public static final class InvalidDateFieldException extends IllegalArgumentException {
        public InvalidDateFieldException(String field, String value, Throwable cause) { super("Неверная дата поля " + field + ": " + value, cause); }
    }
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    public static LocalDate parseDate(String field, String value) {
        Objects.requireNonNull(field); Objects.requireNonNull(value);
        try { return LocalDate.parse(value, DATE); }
        catch (DateTimeParseException e) { throw new InvalidDateFieldException(field, value, e); }
    }
    public static final class InvalidLocalTimeException extends IllegalArgumentException {
        public InvalidLocalTimeException(String message) { super(message); }
    }
    /** Для gap запрещаем время; для overlap явно выбираем первый или второй offset. */
    public static ZonedDateTime resolve(LocalDateTime local, ZoneId zone, boolean secondOffset) {
        ZoneRules rules = zone.getRules(); List<ZoneOffset> offsets = rules.getValidOffsets(local);
        if (offsets.isEmpty()) throw new InvalidLocalTimeException("Локальное время не существует: " + local + " " + zone);
        return ZonedDateTime.ofStrict(local, offsets.get(secondOffset && offsets.size() > 1 ? 1 : 0), zone);
    }
    public record CourseTitle(String display, String searchKey) {
        public static CourseTitle of(String raw) {
            String display = Objects.requireNonNull(raw).strip();
            if (display.isEmpty()) throw new IllegalArgumentException("Пустое название");
            String key = display.replaceAll("(?U)\\s+", " ").toLowerCase(Locale.ROOT);
            return new CourseTitle(display, key);
        }
    }
    /** Фиксируем сумму в минимальных единицах валюты; операции не принимают double. */
    public record Money(BigDecimal amount, Currency currency) {
        public Money {
            Objects.requireNonNull(amount); Objects.requireNonNull(currency);
            int scale = currency.getDefaultFractionDigits();
            if (scale < 0) throw new IllegalArgumentException("Валюта без масштаба");
            amount = amount.setScale(scale, RoundingMode.UNNECESSARY);
        }
        private void same(Money other) { if (!currency.equals(other.currency)) throw new IllegalArgumentException("Разные валюты"); }
        public Money add(Money other) { same(other); return new Money(amount.add(other.amount), currency); }
        public Money subtract(Money other) { same(other); return new Money(amount.subtract(other.amount), currency); }
        public Money percentage(BigDecimal percent, RoundingMode rounding) {
            return new Money(amount.multiply(percent).divide(new BigDecimal("100"), currency.getDefaultFractionDigits(), rounding), currency);
        }
        /** Деление на равные доли: оставшиеся минимальные единицы идут первым позициям. */
        public List<Money> allocate(int parts) {
            if (parts <= 0 || amount.signum() < 0) throw new IllegalArgumentException("Некорректное распределение");
            BigInteger units = amount.movePointRight(currency.getDefaultFractionDigits()).toBigIntegerExact();
            BigInteger[] qr = units.divideAndRemainder(BigInteger.valueOf(parts)); List<Money> out = new ArrayList<>();
            for (int i = 0; i < parts; i++) {
                BigInteger n = qr[0].add(i < qr[1].intValueExact() ? BigInteger.ONE : BigInteger.ZERO);
                out.add(new Money(new BigDecimal(n, currency.getDefaultFractionDigits()), currency));
            }
            return List.copyOf(out);
        }
    }
    /** Скидка распределяется пропорционально цене; остаток копеек — по индексу. */
    public static List<Money> allocateDiscount(List<Money> prices, Money discount, RoundingMode rounding) {
        Objects.requireNonNull(rounding); if (prices.isEmpty()) throw new IllegalArgumentException("Нет позиций");
        Money total = new Money(BigDecimal.ZERO, discount.currency());
        for (Money p : prices) { if (p.amount().signum() < 0) throw new IllegalArgumentException("Отрицательная цена"); total = total.add(p); }
        if (discount.amount().signum() < 0 || discount.amount().compareTo(total.amount()) > 0) throw new IllegalArgumentException("Некорректная скидка");
        int scale = discount.currency().getDefaultFractionDigits();
        if (total.amount().signum() == 0) return prices.stream().map(p -> new Money(BigDecimal.ZERO, p.currency())).toList();
        List<BigDecimal> amounts = new ArrayList<>(); BigDecimal used = BigDecimal.ZERO;
        for (Money p : prices) {
            BigDecimal share = discount.amount().multiply(p.amount()).divide(total.amount(), scale, rounding);
            amounts.add(share); used = used.add(share);
        }
        BigDecimal unit = BigDecimal.ONE.movePointLeft(scale), delta = discount.amount().subtract(used);
        // Коррекция не выводит долю за пределы [0, цена], даже при округлении вверх.
        while (delta.signum() != 0) {
            boolean changed = false;
            for (int i = 0; i < amounts.size() && delta.signum() != 0; i++) {
                BigDecimal step = delta.signum() > 0 ? unit : unit.negate(); BigDecimal next = amounts.get(i).add(step);
                if (next.signum() >= 0 && next.compareTo(prices.get(i).amount()) <= 0) { amounts.set(i, next); delta = delta.subtract(step); changed = true; }
            }
            if (!changed) throw new IllegalStateException("Невозможно распределить остаток");
        }
        return amounts.stream().map(a -> new Money(a, discount.currency())).toList();
    }
    public static List<Integer> randomSample(RandomGenerator random, int size, int lower, int upperExclusive) {
        if (size < 0 || lower >= upperExclusive) throw new IllegalArgumentException("Неверные границы");
        List<Integer> values = new ArrayList<>(); for (int i = 0; i < size; i++) values.add(random.nextInt(lower, upperExclusive)); return List.copyOf(values);
    }
    public record WebinarSeriesPlan(ZoneId zone, List<Instant> sessions) {
        public WebinarSeriesPlan { Objects.requireNonNull(zone); sessions = List.copyOf(sessions); }
        public static WebinarSeriesPlan weekly(LocalDateTime first, ZoneId zone, int count) {
            if (count <= 0) throw new IllegalArgumentException("Нет занятий");
            List<Instant> sessions = new ArrayList<>();
            for (int i = 0; i < count; i++) sessions.add(resolve(first.plusWeeks(i), zone, false).toInstant());
            return new WebinarSeriesPlan(zone, sessions);
        }
    }
    public record Purchase(Money paid, WebinarSeriesPlan plan) {}
    public static Money price(Money base, BigDecimal discountPercent, Instant now, Instant deadline, RoundingMode rounding) {
        if (discountPercent.signum() < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("Скидка вне 0..100");
        return now.isBefore(deadline) ? base.subtract(base.percentage(discountPercent, rounding)) : base;
    }
    public static Money refund(Purchase purchase, Instant now, RoundingMode rounding) {
        long remaining = purchase.plan().sessions().stream().filter(s -> s.isAfter(now)).count();
        int total = purchase.plan().sessions().size();
        if (total == 0) throw new IllegalArgumentException("Нет занятий");
        Money paid = purchase.paid();
        return new Money(paid.amount().multiply(BigDecimal.valueOf(remaining)).divide(BigDecimal.valueOf(total), paid.currency().getDefaultFractionDigits(), rounding), paid.currency());
    }
    public static void main(String[] args) {
        String a = "Java", b = "Ja" + "va", c = new String("Java");
        System.out.println("literal == constant: " + (a == b) + "; literal == new: " + (a == c) + "; equals: " + a.equals(c));
        System.out.println("Код: " + CourseCode.parse("JAVA-101"));
        System.out.println("Дата: " + parseDate("start", "29.02.2024"));
        Money price = new Money(new BigDecimal("100.00"), Currency.getInstance("RUB"));
        System.out.println("Доли: " + price.allocate(3));
    }
}
