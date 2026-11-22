package coursehub.practice6;

import java.math.*;
import java.util.*;
/** Распределяет скидку между позициями с явным округлением. */
public final class DiscountAllocator {
    private DiscountAllocator() {}
    /** Распределяет скидку по ценам и детерминированно учитывает остаток. */
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
}
