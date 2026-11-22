package coursehub.practice3;

import java.util.*;
/** Формирует сочетания форматов проведения и вариантов оплаты. */
public final class Offerings {
    private Offerings() {}
    /** Возвращает все сочетания независимых формата и оплаты. */
    public static List<Offering> combinations() {
        List<Offering> offerings = new ArrayList<>();
        for (DeliveryFormat format : DeliveryFormat.values())
            for (PaymentPlan payment : PaymentPlan.values()) offerings.add(new Offering(format, payment));
        return List.copyOf(offerings);
    }
}
