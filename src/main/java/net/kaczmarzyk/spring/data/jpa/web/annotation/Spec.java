/**
 * Copyright 2014-2025 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.domain.PathSpecification;


/**
 * @author Tomasz Kaczmarzyk
 * @author Matt S.Y. Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.TYPE })
public @interface Spec {

    /**
     * <p>HTTP query parameter name (or names) to be used for matching with entity attributes.</p>
     *
     * <p>For example, setting {@code params} to "name" will mean that you expect {@code ?name=Value} in upcoming HTTP requests.</p>
     *
     * <p>If not set (and {@code headers} and {@code pathVars} are not used), then defaults to the same name as the filtered attribute of the entity (see {@code path}).
     */
    String[] params() default {};

    /**
     * Character used to param delimitation. Param delimitation is skipped when separator has a 0 value.
     */
    char paramSeparator() default 0;

    String[] pathVars() default {};

    String[] headers() default {};

    /**
     * Please remember about adding required Maven dependenecy to your project before using jsonPaths. See <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a> for details.
     */
    String[] jsonPaths() default {};
    
    String[] config() default {};
    
    /**
     * The constant value designed for specification without related HTTP param. Const value can be a raw string or SpEL expression.
     * SpEL expression require {@code valueInSpEL} to be set to {@code true} and {@code SpecificationArgumentResolver} to be properly
     * with the {@code AbstractApplicationContext}, see <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a> for details.
     * <p>Supplying {@link #constVal} implicitly sets {@link #defaultVal} to empty
     */
    String[] constVal() default {};
    
    /**
     * The default value to use as a fallback when the request parameter is
     * not provided or has an empty value. Default value can be a raw string or SpEL expression (could  SpecificationArgumentResolver.
     * SpEL expression require {@code valueInSpEL} to be set to {@code true} and {@code SpecificationArgumentResolver} to be properly
     * with the {@code AbstractApplicationContext}, see <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a> for details.
     * <p>Supplying {@link #constVal} implicitly sets {@link #defaultVal} to empty
     */
    String[] defaultVal() default {};

    /**
     * Attribute determines that constVal/defaultVal value is in SpEL (Spring Expression Language) format.
     * Attribute is ignored when SpEL support is disabled, see <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a>
     * to get more information about enabling/disabling SpEL support.
     */
    boolean valueInSpEL() default false;
    
    /**
     * Attribute determines that provided params value is in SpEL (Spring Expression Language) format. 
     * 
     * Attribute is ignored when SpEL support is disabled, see <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a>
     * to get more information about enabling/disabling SpEL support.
     */
    boolean paramsInSpEL() default false;

    OnTypeMismatch onTypeMismatch() default OnTypeMismatch.EMPTY_RESULT;
    
    /**
     * <p>Attribute name (or more generally, path in the entity graph) to be filtered.</p>
     *
     * <p>
     * For example, consider a {@code Customer} entity with field {@code String firstName}.
     * If you want to filter customers by id, set {@code @Spec.path} attribute to "id".
     * </p>
     */
    String path();

    /**
     * Attribute is used to specify behaviour on missing path variable.
     * Missing param policy should be set to IGNORE when controller endpoint contains multiple paths with different path variables.
     * For more details see <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md#support-for-multiple-paths-with-different-path-variables">README.md</a>
     */
    MissingPathVarPolicy missingPathVarPolicy() default MissingPathVarPolicy.EXCEPTION;

    /**
     * Type of the filter to apply. This should be class that implements {@link Specification} interface.
     * Use one of the built-in classes or implement your own. Built-in classes typically extend {@link PathSpecification}
     * and are described in <a href="https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md">README.md</a>
     *
     * @see PathSpecification
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Specification> spec();

}
