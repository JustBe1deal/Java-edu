package coursehub.practice6;

import java.math.*;
import java.time.*;
/** Применяет скидку до заданного момента. */
public final class PricingService {
    private PricingService() {}
    /** Рассчитывает цену со скидкой до момента окончания акции. */
    public static Money price(Money base, BigDecimal discountPercent, Instant now, Instant deadline, RoundingMode rounding) {
        if (discountPercent.signum() < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("Скидка вне 0..100");
        return now.isBefore(deadline) ? base.subtract(base.percentage(discountPercent, rounding)) : base;
    }
}
