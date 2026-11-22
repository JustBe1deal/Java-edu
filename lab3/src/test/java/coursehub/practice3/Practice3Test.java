package coursehub.practice3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class Practice3Test {
 @Test void dispatchAndFormatting(){ var c=new Course(1,"Java",72); CourseFormatter[] f={new CompactCourseFormatter(),new UpperCourseFormatter()}; assertEquals(List.of("Java","JAVA"),Formatting.formatAll(c,f)); }
 @Test void rulesCompose(){ var p=EnrollmentPolicies.allOf(List.of(new AgePolicy(18),new CapacityPolicy())); var a=new Applicant(20,Set.of(),false,true,true); assertInstanceOf(WaitListed.class,p.evaluate(a)); }
 @Test void errorOverridesWaitlist(){ var p=EnrollmentPolicies.allOf(List.of(new CapacityPolicy(),new AgePolicy(18))); var a=new Applicant(12,Set.of(),false,true,true); assertInstanceOf(Rejected.class,p.evaluate(a)); }
 @Test void anyAccepts(){ var p=EnrollmentPolicies.anyOf(List.of(new AgePolicy(18),new PaymentPolicy())); assertInstanceOf(Accepted.class,p.evaluate(new Applicant(12,Set.of(),false,true,true))); }
}
