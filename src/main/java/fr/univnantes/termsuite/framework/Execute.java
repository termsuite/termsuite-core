package fr.univnantes.termsuite.framework;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

@BindingAnnotation @Target({ METHOD }) @Retention(RUNTIME)
public @interface Execute {
}
