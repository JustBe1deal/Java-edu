package coursehub.practice5;

import java.util.*;
public record Course(int id, String title, Set<String> tags) {
    public Course { Objects.requireNonNull(title); tags = Set.copyOf(tags); }
}
