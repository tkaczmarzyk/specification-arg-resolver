/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.web;

import java.lang.annotation.Annotation;

import org.springframework.core.MethodParameter;


public class MethodParameterUtil {

	public static boolean isAnnotatedWith(Class<? extends Annotation> annotation, MethodParameter param) {
		return param.hasParameterAnnotation(annotation) || param.getParameterType().isAnnotationPresent(annotation);
	}
	
	public static <A extends Annotation> A getAnnotation(Class<A> annotationType, MethodParameter parameter) {
        A annotation = parameter.getParameterAnnotation(annotationType);
        if (annotation == null) {
            annotation = parameter.getParameterType().getAnnotation(annotationType);
        }
        return annotation;
    }
}
