package net.kaczmarzyk.spring.data.jpa.swagger;

import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static net.kaczmarzyk.spring.data.jpa.swagger.SpecExtractorUtil.extractNestedSpecificationsFromAnnotations;
import static org.assertj.core.api.Assertions.assertThat;

public class SpecExtractorUtilTest {

	@Test
	public void shouldExtractSpecsFromAnnotations() throws NoSuchMethodException {
		// given
		Annotation disjunctionAnnotation = fetchAnnotationFromFirstMethodParameter("methodWithDisjunction");
		Annotation conjunctionAnnotation = fetchAnnotationFromFirstMethodParameter("methodWithConjunction");
		Annotation andAnnotation = fetchAnnotationFromFirstMethodParameter("methodWithAnd");
		Annotation orAnnotation = fetchAnnotationFromFirstMethodParameter("methodWithOr");
		Annotation specAnnotation = fetchAnnotationFromFirstMethodParameter("methodWithSpec");

		List<Annotation> annotationsWithSpecs = Arrays.asList(disjunctionAnnotation, conjunctionAnnotation, andAnnotation, orAnnotation, specAnnotation);

		// when
		List<Spec> specs = extractNestedSpecificationsFromAnnotations(annotationsWithSpecs);

		// then
		List<String> specsPaths = specs.stream()
			.map(Spec::path)
			.collect(toList());

		assertThat(specsPaths).contains(
			"disjunctionSpecA", "disjunctionSpecB", "disjunctionSpecC",
			"conjunctionSpecA", "conjunctionSpecB", "conjunctionSpecC",
			"andSpecA", "andSpecB", "orSpecA", "orSpecB", "specSpecA");
	}

	private Annotation fetchAnnotationFromFirstMethodParameter(String methodName) throws NoSuchMethodException {
		return this.getClass()
			.getDeclaredMethod(methodName, Specification.class)
			.getParameterAnnotations()[0][0];
	}

	private void methodWithDisjunction(
		@Disjunction(
			or = @Spec(path = "disjunctionSpecA", spec = Like.class),
			value = @And({
				@Spec(path = "disjunctionSpecB", spec = Like.class),
				@Spec(path = "disjunctionSpecC", spec = Like.class)
			})) Specification<Object> specification) {

	}

	private void methodWithConjunction(
		@Conjunction(
			and = @Spec(path = "conjunctionSpecA", spec = Like.class),
			value = @Or({
				@Spec(path = "conjunctionSpecB", spec = Like.class),
				@Spec(path = "conjunctionSpecC", spec = Like.class)
			})) Specification<Object> specification) {

	}

	private void methodWithAnd(
		@And({
			@Spec(path = "andSpecA", spec = Like.class),
			@Spec(path = "andSpecB", spec = Like.class)
		}) Specification<Object> spec) {

	}

	private void methodWithOr(
		@Or({
			@Spec(path = "orSpecA", spec = Like.class),
			@Spec(path = "orSpecB", spec = Like.class)
		}) Specification<Object> spec) {

	}

	private void methodWithSpec(@Spec(path = "specSpecA", spec = Like.class) Specification<Object> spec) {

	}


}