package cs603.hw3;

import java.lang.annotation.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.StringUtils;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("Periodic")
@interface Periodic {
    int period() default 1;
}

@Periodic(period = 2)
public class TempTest  {
    @Test
    @ExtendWith(PeriodicEnabling.class)
    public void testTrue() {
        assertTrue(true);
    }

    @Test
    @ExtendWith(PeriodicEnabling.class)
    public void testZero() {
        int val = 0; assertEquals(0, val);
    }

    @Test
    @ExtendWith(PeriodicEnabling.class)
    public void testZero2() {
        assertEquals(0, "".length());
    }

    @Test
    @ExtendWith(PeriodicEnabling.class)
    public void testFalse() {
        assertTrue(!false);
    }
}

class PeriodicEnabling implements ExecutionCondition, BeforeTestExecutionCallback {
    private int period;
    static private int cnt = 1;
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled("Enable");

    public PeriodicEnabling() {
        Class<TempTest > cls = TempTest .class;
        if(cls.isAnnotationPresent(Periodic.class)) {
            Annotation annotation = cls.getAnnotation(Periodic.class);
            period = ((Periodic) annotation).period();
        }
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        Optional<Disabled> disabled = findAnnotation(element, Disabled.class);
        if (cnt % period != 0) {
            String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElseGet(
                    () -> element.get() + " is @Disabled");
            cnt = (cnt + 1) % period;
            return ConditionEvaluationResult.disabled(reason);
        }
        else
            return ENABLED;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        cnt = (cnt + 1) % period;
    }
}