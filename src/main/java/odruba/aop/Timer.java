package odruba.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Component;

//@Aspect
@Component
public class Timer {

    @Around("execution(* rdf2vis..*(..))")
    public Object testAspect(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.nanoTime();

        System.out.println("> " + pjp.getSignature());
        Object returnValue = pjp.proceed();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("> Duration: " + duration + " ms");

        return returnValue;
    }

    /*
    @Before("execution(* rdf2vis.VisRDFBuilder.*(..))")
    public void logName() {
        System.out.println("Name....");
    }
    */
}
