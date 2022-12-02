package net.kaczmarzyk.spring.data.jpa.swagger.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class SpecificationArgResolverSpringdocOperationCustomizer implements OperationCustomizer {

	private static final String DEFAULT_PARAMETER_TYPE = "string";

	// Static code block below is necessary to ignore parameters of Specification type in controller method
	static {
		SpringDocUtils.getConfig().addRequestWrapperToIgnore(Specification.class);
	}

	private final Map<Class<?>, Function<Annotation, List<Spec>>> nestedSpecAnnotationSuppliers = new HashMap<>();

	{
		nestedSpecAnnotationSuppliers.put(Spec.class, annotation -> singletonList((Spec) annotation));
		nestedSpecAnnotationSuppliers.put(And.class, annotation -> extractSpecsFromAnd((And) annotation));
		nestedSpecAnnotationSuppliers.put(Or.class, annotation -> extractSpecsFromOr((Or) annotation));
		nestedSpecAnnotationSuppliers.put(Conjunction.class, annotation -> extractSpecsFromConjunction((Conjunction) annotation));
		nestedSpecAnnotationSuppliers.put(Disjunction.class, annotation -> extractSpecsFromDisjunction((Disjunction) annotation));
	}

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		if (isNull(operation) || isNull(handlerMethod)) return operation;

		List<String> requiredParameters = extractRequiredParametersFromHandlerMethod(handlerMethod);

		stream(handlerMethod.getMethodParameters())
				.map(this::extractAnnotationsFromMethodParameter)
				.map(this::extractNestedSpecificationsFromAnnotations)
				.map(specs -> createParametersFromSpecs(specs, requiredParameters))
				.flatMap(Collection::stream)
				.collect(toSet())
				.forEach(operation::addParametersItem);

		return operation;
	}

	private List<String> extractRequiredParametersFromHandlerMethod(HandlerMethod handlerMethod) {
		return ofNullable(handlerMethod.getMethodAnnotation(RequestMapping.class))
				.map(RequestMapping::params)
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

	private List<Spec> extractNestedSpecificationsFromAnnotations(List<Annotation> annotations) {

		return annotations.stream()
				.filter(annotation -> nestedSpecAnnotationSuppliers.containsKey(annotation.annotationType()))
				.map(annotation -> nestedSpecAnnotationSuppliers.get(annotation.annotationType())
						.apply(annotation))
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private List<Spec> extractSpecsFromOr(Or or) {
		return asList(or.value());
	}

	private List<Spec> extractSpecsFromAnd(And and) {
		return asList(and.value());
	}

	private List<Spec> extractSpecsFromConjunction(Conjunction conjunction) {
		List<Spec> conjunctionSpecs = new ArrayList<>(asList(conjunction.and()));

		stream(conjunction.value())
				.map(Or::value)
				.flatMap(Arrays::stream)
				.forEach(conjunctionSpecs::add);

		return conjunctionSpecs;
	}

	private List<Spec> extractSpecsFromDisjunction(Disjunction conjunction) {
		List<Spec> disjunctionSpecs = new ArrayList<>(asList(conjunction.or()));

		stream(conjunction.value())
				.map(And::value)
				.flatMap(Arrays::stream)
				.forEach(disjunctionSpecs::add);

		return disjunctionSpecs;
	}

	private List<Parameter> createParametersFromSpecs(List<Spec> specs, List<String> requiredParameters) {
		Schema<String> defaultParamSchema = new Schema<>();
		defaultParamSchema.setType(DEFAULT_PARAMETER_TYPE);

		return specs.stream()
				.map(Spec::params)
				.flatMap(Arrays::stream)
				.map(parameterName -> {
					Parameter specParam = new Parameter();
					specParam.setName(parameterName);
					specParam.setRequired(requiredParameters.contains(parameterName));
					specParam.setIn("query");
					specParam.setSchema(defaultParamSchema);

					return specParam;
				})
				.collect(toList());
	}

}
