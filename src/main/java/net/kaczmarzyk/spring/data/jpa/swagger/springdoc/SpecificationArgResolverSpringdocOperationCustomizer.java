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
package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import net.kaczmarzyk.spring.data.jpa.swagger.SpecExtractorUtil;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationArgResolverSpringdocOperationCustomizer implements OperationCustomizer {

	private static final Schema<String> STRING_PARAMETER_SCHEMA = new StringSchema();

	private static final String JSON_FILTER_PARAMETER_NAME = "filterRequestBody";

	// Static code block below is necessary to ignore parameters of Specification type in controller method
	static {
		SpringDocUtils.getConfig().addRequestWrapperToIgnore(Specification.class);
	}

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		if (isNull(operation) || isNull(handlerMethod)) {
			return operation;
		}

		List<String> requiredParams = extractRequiredParametersFromHandlerMethod(handlerMethod, RequestMapping::params);
		List<String> requiredHeaders = extractRequiredParametersFromHandlerMethod(handlerMethod, RequestMapping::headers);

		stream(handlerMethod.getMethodParameters())
			.map(this::extractAnnotationsFromMethodParameter)
			.map(SpecExtractorUtil::extractNestedSpecificationsFromAnnotations)
			.map(specs -> createParametersFromSpecs(specs, requiredParams, requiredHeaders))
			.flatMap(Collection::stream)
			.distinct()
			.forEach(parameter -> addOrUpdateParameter(operation, parameter));

		return operation;
	}

	private void addOrUpdateParameter(Operation operation, Parameter parameter) {
		if (nonNull(operation.getParameters())) {
			operation.getParameters().removeIf(operationParameter ->
				parametersRepresentTheSameProperty(operationParameter, parameter));
		}
		operation.addParametersItem(parameter);
	}

	private boolean parametersRepresentTheSameProperty(Parameter firstParameter, Parameter secondParameter) {
		return Objects.equals(firstParameter.getName(), secondParameter.getName()) && Objects.equals(firstParameter.getIn(), secondParameter.getIn());
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

    extractAnnotationsRecursive(annotations, methodParameter.getParameterType());

		return annotations;
	}

  private void extractAnnotationsRecursive(List<Annotation> buffer, Class<?> clazz) {
    if (clazz != Specification.class) {
      List<Annotation> innerParameterAnnotations = asList(clazz.getAnnotations());
      buffer.addAll(innerParameterAnnotations);

      for (Class<?> extendedInterface : clazz.getInterfaces()) {
        extractAnnotationsRecursive(buffer, extendedInterface);
      }
    }
  }

	private List<Parameter> createParametersFromSpecs(List<Spec> specs, List<String> requiredParams, List<String> requiredHeaders) {
		List<Parameter> parameters = specs.stream()
			.map(spec -> {
				List<Parameter> specParameters = new ArrayList<>();
				specParameters.addAll(createParameters(spec.params(), requiredParams, QUERY));
				specParameters.addAll(createParameters(spec.pathVars(), emptyList(), PATH));
				specParameters.addAll(createParameters(spec.headers(), requiredHeaders, HEADER));

				return specParameters;
			})
			.flatMap(Collection::stream)
			.collect(toList());

		createJsonPathsParameterFromSpecs(specs).ifPresent(parameters::add);

		return parameters;
	}

	private List<Parameter> createParameters(String[] parameters, List<String> requiredParameters, ParameterIn parameterIn) {
		return stream(parameters)
			.map(parameterName -> {
				Parameter specParam = generateParameterFromParameterIn(parameterIn);
				specParam.setName(parameterName);
				specParam.setRequired(requiredParameters.contains(parameterName) || parameterIn == PATH);
				specParam.setSchema(STRING_PARAMETER_SCHEMA);

				return specParam;
			})
			.collect(toList());
	}

	private Optional<Parameter> createJsonPathsParameterFromSpecs(List<Spec> specs) {

		List<String> jsonPaths = specs.stream()
			.map(Spec::jsonPaths)
			.flatMap(Arrays::stream)
			.collect(toList());

		if (jsonPaths.isEmpty()) {
			return empty();
		}

		Parameter jsonParameter = generateParameterFromParameterIn(QUERY);
		jsonParameter.setName(JSON_FILTER_PARAMETER_NAME);

		Map<String, Schema> schemasForJsonPaths = preparePropertiesMapFromJsonPaths(jsonPaths);
		Schema<Object> parameterSchema = new ObjectSchema();
		parameterSchema.setProperties(schemasForJsonPaths);

		jsonParameter.setSchema(parameterSchema);
		jsonParameter.setRequired(false);

		return Optional.of(jsonParameter);
	}

	private Map<String, Schema> preparePropertiesMapFromJsonPaths(List<String> jsonPaths) {
		Map<String, Schema> ejectedPaths = new HashMap<>();

		jsonPaths.forEach(jsonPath -> {
			String[] partsOfJsonPath = jsonPath.split("\\.");
			Iterator<String> jsonPathIterator = stream(partsOfJsonPath).iterator();

			// initialize schemas on the main JSON level
			String firstPartOfPath = jsonPathIterator.next();
			if (!ejectedPaths.containsKey(firstPartOfPath)) {
				Schema<?> jsonSchema = jsonPathIterator.hasNext() ? new ObjectSchema() : STRING_PARAMETER_SCHEMA;
				ejectedPaths.put(firstPartOfPath, jsonSchema);
			}

			// build schemas for nested JSON objects
			Schema<?> previousSchema = ejectedPaths.get(firstPartOfPath);
			while (jsonPathIterator.hasNext()) {
				String nextJsonPath = jsonPathIterator.next();

				if (schemaDoesNotContainParameter(previousSchema, nextJsonPath)) {
					Schema<?> jsonSchema = jsonPathIterator.hasNext() ? new ObjectSchema() : STRING_PARAMETER_SCHEMA;
					previousSchema.addProperty(nextJsonPath, jsonSchema);
				}
				previousSchema = (Schema<?>) previousSchema.getProperties().get(nextJsonPath);
			}
		});

		return ejectedPaths;
	}

	private boolean schemaDoesNotContainParameter(Schema<?> schema, String parameterName) {
		return isNull(schema.getProperties()) || !schema.getProperties().containsKey(parameterName);
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
