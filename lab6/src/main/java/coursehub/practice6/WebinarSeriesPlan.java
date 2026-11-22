package coursehub.practice6;

import java.time.*;
import java.util.*;
public record WebinarSeriesPlan(ZoneId zone, List<Instant> sessions) {
    public WebinarSeriesPlan { Objects.requireNonNull(zone); sessions = List.copyOf(sessions); }
    public static WebinarSeriesPlan weekly(LocalDateTime first, ZoneId zone, int count) {
        if (count <= 0) throw new IllegalArgumentException("Нет занятий");
        List<Instant> sessions = new ArrayList<>();
        for (int i = 0; i < count; i++) sessions.add(LocalDateTimeResolver.resolve(first.plusWeeks(i), zone, false).toInstant());
        return new WebinarSeriesPlan(zone, sessions);
    }
}
