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
package net.kaczmarzyk.spring.data.jpa.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.jpa.domain.Specification;


/**
 * @author Tomasz Kaczmarzyk
 * @author Matt S.Y. Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.TYPE })
public @interface Spec {

    String[] params() default {};

    /**
     * Character used to param delimitation. Param delimitation is skipped when separator has a 0 value.
     */
    char paramSeparator() default 0;

    String[] pathVars() default {};
    
    String[] config() default {};
    
    String[] constVal() default {};
    
    /**
     * The default value to use as a fallback when the request parameter is
     * not provided or has an empty value.
     * <p>Supplying {@link #constVal} implicitly sets {@link #defaultVal} to empty
     */
    String[] defaultVal() default {};
    
    OnTypeMismatch onTypeMismatch() default OnTypeMismatch.EMPTY_RESULT;
    
    String path();
    
    @SuppressWarnings("rawtypes")
    Class<? extends Specification> spec();
}
