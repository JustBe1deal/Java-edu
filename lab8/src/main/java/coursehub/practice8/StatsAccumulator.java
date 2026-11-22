package coursehub.practice8;

import java.util.*;
/** Временный аккумулятор для собственного коллектора статистики. */
final class StatsAccumulator {
    long count, sum; int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    void add(Course c) { count = Math.addExact(count,1); sum = Math.addExact(sum,c.duration()); min = Math.min(min,c.duration()); max = Math.max(max,c.duration()); }
    StatsAccumulator merge(StatsAccumulator b) { count = Math.addExact(count,b.count); sum = Math.addExact(sum,b.sum); min = Math.min(min,b.min); max = Math.max(max,b.max); return this; }
    Stats finish() { return new Stats(count,sum,count == 0 ? OptionalInt.empty() : OptionalInt.of(min),count == 0 ? OptionalInt.empty() : OptionalInt.of(max)); }
}
