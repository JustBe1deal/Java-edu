package coursehub.practice6;

import java.math.*;
import java.util.*;
/** Неизменяемая сумма с валютой и явными правилами округления. */
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
