package coursehub.practice5;

import java.util.*;
public record CourseCode(String value) { public CourseCode { Objects.requireNonNull(value); } }
