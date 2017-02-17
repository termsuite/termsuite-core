package fr.univnantes.termsuite.framework;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import fr.univnantes.termsuite.uima.ResourceType;

@BindingAnnotation @Target({ FIELD }) @Retention(RUNTIME)
public @interface Resource {
	ResourceType type();
}
