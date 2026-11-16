package coursehub.practice3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class Practice3Test {
 @Test void dispatchAndFormatting(){ var c=new Practice3.Course(1,"Java",72); Practice3.CourseFormatter[] f={new Practice3.CompactCourseFormatter(),new Practice3.UpperCourseFormatter()}; assertEquals(List.of("Java","JAVA"),Practice3.formatAll(c,f)); }
 @Test void rulesCompose(){ var p=Practice3.allOf(List.of(new Practice3.AgePolicy(18),new Practice3.CapacityPolicy())); var a=new Practice3.Applicant(20,Set.of(),false,true,true); assertInstanceOf(Practice3.WaitListed.class,p.evaluate(a)); }
 @Test void errorOverridesWaitlist(){ var p=Practice3.allOf(List.of(new Practice3.CapacityPolicy(),new Practice3.AgePolicy(18))); var a=new Practice3.Applicant(12,Set.of(),false,true,true); assertInstanceOf(Practice3.Rejected.class,p.evaluate(a)); }
 @Test void anyAccepts(){ var p=Practice3.anyOf(List.of(new Practice3.AgePolicy(18),new Practice3.PaymentPolicy())); assertInstanceOf(Practice3.Accepted.class,p.evaluate(new Practice3.Applicant(12,Set.of(),false,true,true))); }
}
