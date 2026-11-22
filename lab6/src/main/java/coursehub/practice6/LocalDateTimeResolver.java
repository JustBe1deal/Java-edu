package coursehub.practice6;

import java.time.*;
import java.time.zone.ZoneRules;
import java.util.*;
/** Определяет момент в зоне с учётом пропусков и повторов времени. */
public final class LocalDateTimeResolver {
    private LocalDateTimeResolver() {}
    /** Отклоняет пропущенное местное время и явно выбирает offset при повторе. */
    public static ZonedDateTime resolve(LocalDateTime local, ZoneId zone, boolean secondOffset) {
        ZoneRules rules = zone.getRules(); List<ZoneOffset> offsets = rules.getValidOffsets(local);
        if (offsets.isEmpty()) throw new InvalidLocalTimeException("Локальное время не существует: " + local + " " + zone);
        return ZonedDateTime.ofStrict(local, offsets.get(secondOffset && offsets.size() > 1 ? 1 : 0), zone);
    }
}
