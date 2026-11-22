package coursehub.practice6;

import java.math.*;
import java.time.*;
/** Рассчитывает возврат по числу оставшихся занятий. */
public final class RefundService {
    private RefundService() {}
    /** Рассчитывает возврат пропорционально будущим занятиям. */
    public static Money refund(Purchase purchase, Instant now, RoundingMode rounding) {
        long remaining = purchase.plan().sessions().stream().filter(s -> s.isAfter(now)).count();
        int total = purchase.plan().sessions().size();
        if (total == 0) throw new IllegalArgumentException("Нет занятий");
        Money paid = purchase.paid();
        return new Money(paid.amount().multiply(BigDecimal.valueOf(remaining)).divide(BigDecimal.valueOf(total), paid.currency().getDefaultFractionDigits(), rounding), paid.currency());
    }
}
