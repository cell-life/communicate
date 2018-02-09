package org.celllife.mobilisr.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks classes that should be excluded from proccesing
 * by the test Spring application context.
 * 
 * @see src/test/resource/applicationContext.xml
 * 
 * @author Simon Kelly
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextIgnore { 
}
