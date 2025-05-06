package io.hynix.units.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface UnitRegister {
    String name();
    int key() default 0;
    Category category();
    String desc() default "";
    boolean premium() default false;
}
