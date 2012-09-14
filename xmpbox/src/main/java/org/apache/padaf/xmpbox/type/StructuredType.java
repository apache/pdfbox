package org.apache.padaf.xmpbox.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StructuredType {

	String namespace ();
	
	String preferedPrefix();
	
}
