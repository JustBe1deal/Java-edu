package coursehub.practice3;

import java.util.*;
public record Applicant(int age, Set<String> completed, boolean hasSeat, boolean paid, boolean active) {
    public Applicant { completed = Set.copyOf(completed); }
}
