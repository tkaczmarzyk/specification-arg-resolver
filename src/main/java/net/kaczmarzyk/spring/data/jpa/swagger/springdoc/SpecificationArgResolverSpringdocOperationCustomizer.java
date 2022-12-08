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
package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import net.kaczmarzyk.spring.data.jpa.swagger.SpecExtractorUtil;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.*;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationArgResolverSpringdocOperationCustomizer implements OperationCustomizer {

	private static final Schema<String> DEFAULT_PARAMETER_SCHEMA = new StringSchema();

	// Static code block below is necessary to ignore parameters of Specification type in controller method
	static {
		SpringDocUtils.getConfig().addRequestWrapperToIgnore(Specification.class);
	}

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		if (isNull(operation) || isNull(handlerMethod)) return operation;

		List<String> requiredParams = extractRequiredParametersFromHandlerMethod(handlerMethod, RequestMapping::params);
		List<String> requiredHeaders = extractRequiredParametersFromHandlerMethod(handlerMethod, RequestMapping::headers);

		stream(handlerMethod.getMethodParameters())
			.map(this::extractAnnotationsFromMethodParameter)
			.map(SpecExtractorUtil::extractNestedSpecificationsFromAnnotations)
			.map(specs -> createParametersFromSpecs(specs, requiredParams, requiredHeaders))
			.flatMap(Collection::stream)
			.distinct()
			.filter(parameter -> parameterWasNotGeneratedAutomatically(operation, parameter))
			.forEach(operation::addParametersItem);

		return operation;
	}

	private boolean parameterWasNotGeneratedAutomatically(Operation operation, Parameter parameter) {
		if (isNull(operation.getParameters())) return true;

		return operation.getParameters().stream()
			.noneMatch(operationParam ->
				Objects.equals(operationParam.getName(), parameter.getName()) && Objects.equals(operationParam.getIn(), parameter.getIn()));
	}

	private List<String> extractRequiredParametersFromHandlerMethod(HandlerMethod handlerMethod, Function<RequestMapping, String[]> parametersFetchingFunction) {
		return ofNullable(handlerMethod.getMethodAnnotation(RequestMapping.class))
			.map(parametersFetchingFunction)
			.map(Arrays::asList)
			.orElse(emptyList());
	}

	private List<Annotation> extractAnnotationsFromMethodParameter(MethodParameter methodParameter) {

		List<Annotation> annotations = new ArrayList<>(
			asList(methodParameter.getParameterAnnotations()));

		if (methodParameter.getParameterType() != Specification.class) {
			List<Annotation> innerParameterAnnotations = asList(methodParameter.getParameterType().getAnnotations());
			annotations.addAll(innerParameterAnnotations);
		}

		return annotations;
	}

	private List<Parameter> createParametersFromSpecs(List<Spec> specs, List<String> requiredParams, List<String> requiredHeaders) {

		return specs.stream()
			.map(spec -> {
				List<Parameter> specParameters = new ArrayList<>();
				specParameters.addAll(createParameters(spec.params(), requiredParams, QUERY));
				specParameters.addAll(createParameters(spec.pathVars(), emptyList(), PATH));
				specParameters.addAll(createParameters(spec.headers(), requiredHeaders, HEADER));

				return specParameters;
			})
			.flatMap(Collection::stream)
			.collect(toList());
	}

	private List<Parameter> createParameters(String[] parameters, List<String> requiredParameters, ParameterIn parameterIn) {
		return stream(parameters)
			.map(parameterName -> {
				Parameter specParam = generateParameterFromParameterIn(parameterIn);
				specParam.setName(parameterName);
				specParam.setRequired(requiredParameters.contains(parameterName) || parameterIn == PATH);
				specParam.setSchema(DEFAULT_PARAMETER_SCHEMA);

				return specParam;
			})
			.collect(toList());
	}

	private Parameter generateParameterFromParameterIn(ParameterIn parameterIn) {
		if (parameterIn == PATH) {
			return new PathParameter();
		} else if (parameterIn == HEADER) {
			return new HeaderParameter();
		}
		return new QueryParameter();
	}

}
