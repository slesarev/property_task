import java.lang.annotation.*;
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Property {
    String propertyName();
    String def() default "";
}
