/**
 * author: Marcel Genzmehr
 * 26.07.2011
 */
package org.freeplane.plugin.workspace.io.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface ExportAsAttribute {
	String name();
	boolean defaultBool() default false;
}
