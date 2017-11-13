package net.kaczmarzyk.spring.data.jpa.web;

import java.lang.annotation.Annotation;

import org.springframework.core.MethodParameter;


public class MethodParameterUtil {

	public static boolean isAnnotatedWith(Class<? extends Annotation> annotation, MethodParameter param) {
		return param.hasParameterAnnotation(annotation) || param.getParameterType().isAnnotationPresent(annotation);
	}
}
