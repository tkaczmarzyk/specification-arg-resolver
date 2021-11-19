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


import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * @author Tomasz Kaczmarzyk
 */
public class AnnotatedSpecInterfaceArgumentResolverTest extends AnnotatedSpecInterfaceTestBase {

	SpecificationArgumentResolver resolver = new SpecificationArgumentResolver();

	public static interface IfaceWithoutAnnotations extends Specification<Object> {
	}

	@Spec(path = "name", spec = Like.class)
	public static interface IfaceWithSimpleSpec extends Specification<Object> {
	}

	@Spec(path = "name", spec = Like.class)
	public static interface IfaceNotExtendingSpecification {
	}

	@And({ @Spec(path = "name", spec = Like.class) })
	public static interface IfaceWithAnd extends Specification<Object> {
	}

	@Conjunction({
			@Or({
					@Spec(path = "firstName", spec = Like.class),
					@Spec(path = "lastName", spec = Like.class)
			}),
			@Or({
					@Spec(path = "gender", spec = Equal.class),
					@Spec(path = "weight", spec = Equal.class)
			})
	})
	public static interface IfaceWithConjunction extends Specification<Object> {
	}

	@Disjunction({
			@And({
					@Spec(path = "firstName", spec = Like.class),
					@Spec(path = "lastName", spec = Like.class)
			}),
			@And({
					@Spec(path = "gender", spec = Equal.class),
					@Spec(path = "weight", spec = Equal.class)
			})
	})
	public static interface IfaceWithDisjunction extends Specification<Object> {
	}

	@And({ @Spec(path = "name", spec = Like.class) })
	@Spec(path = "name", spec = Like.class)
	public static interface OverAnnotatedInterface extends Specification<Object> {
	}

	@Or({ @Spec(path = "name", spec = Like.class) })
	public static interface IfaceWithOr extends Specification<Object> {
	}

	@Spec(path = "name", spec = Like.class)
	public static class Clazz extends Like<Object> {
		public Clazz(QueryContext queryCtx, String path, String[] args) {
			super(queryCtx, path, args);
		}
	}

	@Spec(path = "deleted", spec = Equal.class)
	public static interface BaseInterface extends Specification<Customer> {
	}

	@Or({
			@Spec(path = "name", spec = Like.class),
			@Spec(path = "weight", spec = Equal.class)
	})
	public static interface ChildInterface extends BaseInterface {
	}

	@Or({
			@Spec(path = "name2", spec = Like.class),
			@Spec(path = "weight2", spec = Equal.class)
	})
	public static interface Chil2dInterface extends BaseInterface {
	}

	@Or({
			@Spec(path = "gold", spec = Equal.class),
			@Spec(path = "status", spec = In.class)
	})
	public static interface GrandChildInterface extends ChildInterface, Chil2dInterface {
	}

	Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);

	public static class TestController {
		public void methodWithSimpleSpec(IfaceWithSimpleSpec arg) {}
		public void methodWithClass(Clazz arg) {}
		public void methodWithOverannotatedSpec(OverAnnotatedInterface arg) {}
		public void methodWithSpecWithoutAnnotations(IfaceWithoutAnnotations arg) {}
		public void methodWithNonSpec(IfaceNotExtendingSpecification arg) {}
		public void methodWithAnd(IfaceWithAnd arg) {}
		public void methodWithConjunction(IfaceWithConjunction arg) {}
		public void methodWithOr(IfaceWithOr arg) {}
		public void methodWithDisjunction(IfaceWithDisjunction arg) {}
		public void methodWithInheritanceTree(GrandChildInterface arg) {}
	}

	NativeWebRequest req = mock(NativeWebRequest.class);
	QueryContext queryCtx = new WebRequestQueryContext(req);

	@BeforeEach
	public void init() {
		req = nativeWebRequest()
				.withParameterValues("deleted", "false")
				.withParameterValues("gold", "true")
				.withParameterValues("status", "ACTIVE", "VERY_ACTIVE")
				.withParameterValues("name", "Homer")
				.withParameterValues("name2", "Max")
				.withParameterValues("firstName", "Homer")
				.withParameterValues("lastName", "Simpson")
				.withParameterValues("weight", "121")
				.withParameterValues("weight2", "99")
				.withParameterValues("gender", "MALE").build();
	}

	@Test
	public void doesNotSupportTypeThatDoesntExtendSpecification() {
		MethodParameter param = methodParameter("methodWithNonSpec", IfaceNotExtendingSpecification.class);

		assertFalse(resolver.supportsParameter(param));
	}

	@Test
	public void doesNotSupportIfaceWithoutAnnotationsSpecification() {
		MethodParameter param = methodParameter("methodWithSpecWithoutAnnotations", IfaceWithoutAnnotations.class);

		assertFalse(resolver.supportsParameter(param));
	}

	@Test
	public void doesNotSupportClasses() {
		MethodParameter param = methodParameter("methodWithClass", Clazz.class);

		assertFalse(resolver.supportsParameter(param));
	}

	@Test
	public void supportsIterfaceWithSimpleSpec() throws Exception {
		MethodParameter param = methodParameter("methodWithSimpleSpec", IfaceWithSimpleSpec.class);

		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithSimpleSpec.class);
	}

	@Test
	public void supportsIterfaceWithAndSpec() throws Exception {
		MethodParameter param = methodParameter("methodWithAnd", IfaceWithAnd.class);

		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithAnd.class);
	}

	@Test
	public void supportsInterfaceWithConjunctionSpec() throws Exception {
		MethodParameter param = methodParameter("methodWithConjunction", IfaceWithConjunction.class);

		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithConjunction.class);
	}

	@Test
	public void supportsInterfaceWithDisjunctionSpec() throws Exception {
		MethodParameter param = methodParameter("methodWithDisjunction", IfaceWithDisjunction.class);

		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithDisjunction.class);
	}

	@Test
	public void supportsIterfaceWithDisjunctionSpec() throws Exception {
		MethodParameter param = methodParameter("methodWithOr", IfaceWithOr.class);

		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithOr.class);
	}

	@Test
	public void resolvedSpecHasWorkingToStringMethod() throws Exception {
		MethodParameter param = methodParameter("methodWithOr", IfaceWithOr.class);

		Object resolved = resolver.resolveArgument(param, null, req, null);

		assertThat(resolved.toString()).startsWith("IfaceWithOr[");
	}

	@Test
	public void createsConjunctionOutOfSpecsFromWholeInheritanceTree() throws Exception {
		MethodParameter param = methodParameter("methodWithInheritanceTree", GrandChildInterface.class);

		Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

		Collection<Specification<Object>> resolvedInnerSpecs = innerSpecs(resolved);
		assertThat(resolvedInnerSpecs)
				.hasSize(4)
				.containsOnly(
						new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "deleted", new String[]{ "false" }, converter)),
						new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
								new Like<>(queryCtx, "name", new String[]{ "Homer" }),
								new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "weight", new String[]{ "121" }, converter))),
						new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
								new Like<>(queryCtx, "name2", new String[]{ "Max" }),
								new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "weight2", new String[]{ "99" }, converter))),
						new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
								new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "gold", new String[]{ "true" }, converter)),
								new EmptyResultOnTypeMismatch<>(new In<>(queryCtx, "status", new String[]{ "ACTIVE", "VERY_ACTIVE" }, converter))));
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
