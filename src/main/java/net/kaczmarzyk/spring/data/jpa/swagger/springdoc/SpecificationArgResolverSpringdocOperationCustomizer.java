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
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class SpecificationArgResolverSpringdocOperationCustomizer implements OperationCustomizer {

	// Code below is necessary to ignore parameters of Specification type in controller method
	static {
		SpringDocUtils.getConfig().addRequestWrapperToIgnore(Specification.class);
	}

	private final Map<Class<?>, Function<Annotation, List<Spec>>> nestedSpecAnnotationSuppliers = Map.of(
			And.class, annotation -> extractSpecificationsFromAnd((And) annotation),
			Or.class, annotation -> extractSpecificationsFromOr((Or) annotation),
			Conjunction.class, annotation -> extractSpecificationsFromConjunction((Conjunction) annotation),
			Disjunction.class, annotation -> extractSpecificationsFromDisjunction((Disjunction) annotation)
	);

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		if (isNull(operation) || isNull(handlerMethod)) return operation;

		List<String> requiredParameters = extractRequiredParametersFromHandlerMethod(handlerMethod);

		List<Annotation> annotationsWithNestedSpec = stream(handlerMethod.getMethodParameters())
				.map(MethodParameter::getParameterType)
				.filter(this::methodParameterTypeIsSpecification)
				.map(Class::getAnnotations)
				.map(Arrays::asList)
				.flatMap(Collection::stream)
				.collect(toList());

		annotationsWithNestedSpec.stream()
				.map(this::extractNestedSpecificationsFromAnnotation)
				.map(this::deduplicateSpecificationsByParameterName)
				.flatMap(Collection::stream)
				.map(spec -> createParameters(spec, requiredParameters))
				.flatMap(Collection::stream)
				.forEach(operation::addParametersItem);

		return operation;
	}

	private boolean methodParameterTypeIsSpecification(Class<?> methodParameterType) {
		return stream(methodParameterType.getInterfaces())
				.anyMatch(parameterTypeInterface -> parameterTypeInterface == Specification.class);
	}

	private Collection<Spec> deduplicateSpecificationsByParameterName(List<Spec> specifications) {
		Map<String, Spec> specificationByParameterName = new HashMap<>();
		specifications.forEach(specification ->
				stream(specification.params())
						.forEach(param -> specificationByParameterName.putIfAbsent(param, specification)));

		return specificationByParameterName.values();
	}

	private List<Spec> extractNestedSpecificationsFromAnnotation(Annotation annotation) {
		if (!nestedSpecAnnotationSuppliers.containsKey(annotation.annotationType())) {
			return emptyList();
		}

		return nestedSpecAnnotationSuppliers.get(annotation.annotationType())
				.apply(annotation);
	}

	private List<Spec> extractSpecificationsFromOr(Or or) {
		return asList(or.value());
	}

	private List<Spec> extractSpecificationsFromAnd(And and) {
		return asList(and.value());
	}

	private List<Spec> extractSpecificationsFromConjunction(Conjunction conjunction) {
		List<Spec> conjunctionSpecifications = new ArrayList<>(asList(conjunction.and()));

		stream(conjunction.value())
				.map(Or::value)
				.map(Arrays::asList)
				.flatMap(Collection::stream)
				.forEach(conjunctionSpecifications::add);

		return conjunctionSpecifications;
	}

	private List<Spec> extractSpecificationsFromDisjunction(Disjunction conjunction) {
		List<Spec> disjunctionSpecifications = new ArrayList<>(asList(conjunction.or()));

		stream(conjunction.value())
				.map(And::value)
				.map(Arrays::asList)
				.flatMap(Collection::stream)
				.forEach(disjunctionSpecifications::add);

		return disjunctionSpecifications;
	}

	private List<Parameter> createParameters(Spec spec, List<String> requiredParameters) {
		Schema<String> defaultParamSchema = new Schema<>();
		defaultParamSchema.setType("string");

		return stream(spec.params())
				.map(parameterName -> {
					Parameter specificationParam = new Parameter();
					specificationParam.setName(parameterName);
					specificationParam.setRequired(requiredParameters.contains(spec.path()));
					specificationParam.setIn("query");
					specificationParam.setSchema(defaultParamSchema);

					return specificationParam;
				})
				.collect(toList());
	}

	private List<String> extractRequiredParametersFromHandlerMethod(HandlerMethod handlerMethod) {
		return ofNullable(handlerMethod.getMethodAnnotation(RequestMapping.class))
				.map(RequestMapping::params)
				.map(Arrays::asList)
				.orElse(emptyList());
	}
}
