package coursehub.practice9;

import java.util.*;
public interface Enrollment {
    boolean enroll(String student);
    Set<String> students();
}
