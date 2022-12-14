/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import org.springframework.core.MethodParameter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * *
 * * The primary extraction method extracts resolved variables from request attributes.
 * * However, due to the fact that the presence of resolved variables in request attributes is not guarantee (ref: {@link org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE}
 * * there is a fallback mechanism which is being used in case of missing resolved variables in request attributes.
 * *
 * * The fallback approach, resolves variables by comparison of:
 * * * actual request path - {@link HttpServletRequest#getPathInfo()} or {@link HttpServletRequest#getRequestURI()} (when getPathInfo() is a null)
 * * * path pattern - it is calculated using information included in the annotations: @RequestMapping, @GetMapping on the method and class level.
 * * The fallback mechanism does not support URIs consisting from global prefix defined for example via: {@link org.springframework.web.servlet.config.annotation.PathMatchConfigurer#addPathPrefix PathMatchConfigurer::addPathPrefix}
 *
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class PathVariableResolver {

    public static Map<String, String> resolvePathVariables(NativeWebRequest nativeWebRequest, MethodParameter methodParameter) {
        Map<String, String> pathVariables = (Map) nativeWebRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, WebRequest.SCOPE_REQUEST);

        if (nonNull(pathVariables)) {
            return pathVariables;
        }

        return resolvePathVariables(pathPattern(methodParameter), actualWebPath(nativeWebRequest));
    }

    private static Map<String, String> resolvePathVariables(String pathPattern, String actualPath) {
        PathMatcher pathMatcher = new AntPathMatcher();

        if (pathMatcher.match(pathPattern, actualPath)) {
            return pathMatcher.extractUriTemplateVariables(pathPattern, actualPath);
        } else {
            return emptyMap();
        }
    }

    private static String pathPattern(MethodParameter methodParameter) {
        String pathPattern = null;

        Class<?> controllerClass = methodParameter.getContainingClass();
        if (controllerClass.getAnnotation(RequestMapping.class) != null) {
            RequestMapping controllerMapping = controllerClass.getAnnotation(RequestMapping.class);
            pathPattern = firstOf(controllerMapping.value(), controllerMapping.path());
        }

        String methodPathPattern = null;

        if (methodParameter.hasMethodAnnotation(RequestMapping.class)) {
            RequestMapping methodMapping = methodParameter.getMethodAnnotation(RequestMapping.class);
            methodPathPattern = firstOf(methodMapping.value(), methodMapping.path());
        } else if (methodParameter.hasMethodAnnotation(GetMapping.class)) {
            GetMapping methodMapping = methodParameter.getMethodAnnotation(GetMapping.class);
            methodPathPattern = firstOf(methodMapping.value(), methodMapping.path());
        }

        if (methodPathPattern != null) {
            pathPattern = pathPattern != null ? pathPattern + methodPathPattern : methodPathPattern;
        }

        if (pathPattern.isEmpty()) {
            throw new IllegalStateException("path pattern could not be resolved (searched for @RequestMapping or @GetMapping)");
        }
        return pathPattern;
    }

    private static String actualWebPath(NativeWebRequest nativeWebRequest) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        return request.getPathInfo() != null ? request.getPathInfo() : request.getRequestURI().substring(request.getContextPath().length());
    }

    private static String firstOf(String[] array1, String[] array2) {
        if (array1.length > 0) {
            return array1[0];
        } else if (array2.length > 0) {
            return array2[0];
        }
        return null;
    }
}
