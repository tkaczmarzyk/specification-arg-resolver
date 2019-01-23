/**
 * Copyright 2014-2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.ReflectionUtils;


/**
 * @author Tomasz Kaczmarzyk
 */
public class AnnotatedSpecInterfaceArgumentResolverTest extends ResolverTestBase {

	AnnotatedSpecInterfaceArgumentResolver resolver = new AnnotatedSpecInterfaceArgumentResolver();
	
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
	
	Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT);
	
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
	
	@Before
	public void init() {
		when(req.getParameterValues("deleted")).thenReturn(new String[] { "false" });
		when(req.getParameterValues("gold")).thenReturn(new String[] { "true" });
		when(req.getParameterValues("status")).thenReturn(new String[] { "ACTIVE", "VERY_ACTIVE" });
		when(req.getParameterValues("name")).thenReturn(new String[] { "Homer" });
		when(req.getParameterValues("name2")).thenReturn(new String[] { "Max" });
		when(req.getParameterValues("firstName")).thenReturn(new String[] { "Homer" });
		when(req.getParameterValues("lastName")).thenReturn(new String[] { "Simpson" });
		when(req.getParameterValues("weight")).thenReturn(new String[] { "121" });
		when(req.getParameterValues("weight2")).thenReturn(new String[] { "99" });
		when(req.getParameterValues("gender")).thenReturn(new String[] { "MALE" });
	}
	
	@Test
	public void doesNotSupportTypeThatDoesntExtendSpecification() {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithNonSpec", IfaceNotExtendingSpecification.class), 0);
        
        assertFalse(resolver.supportsParameter(param));
	}
	
	@Test
	public void doesNotSupportIfaceWithoutAnnotationsSpecification() {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithSpecWithoutAnnotations", IfaceWithoutAnnotations.class), 0);
        
        assertFalse(resolver.supportsParameter(param));
	}
	
	@Test
	public void doesNotSupportClasses() {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithClass", Clazz.class), 0);
        
        assertFalse(resolver.supportsParameter(param));
	}
	
	@Test
	public void supportsIterfaceWithSimpleSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithSimpleSpec", IfaceWithSimpleSpec.class), 0);
        
		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithSimpleSpec.class);
	}
	
	@Test
	public void supportsIterfaceWithAndSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithAnd", IfaceWithAnd.class), 0);
        
		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithAnd.class);
	}
	
	@Test
	public void supportsInterfaceWithConjunctionSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithConjunction", IfaceWithConjunction.class), 0);
        
		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithConjunction.class);
	}
	
	@Test
	public void supportsInterfaceWithDisjunctionSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithDisjunction", IfaceWithDisjunction.class), 0);
        
		assertTrue(resolver.supportsParameter(param));
		assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithDisjunction.class);
	}
	
	@Test
	public void supportsIterfaceWithDisjunctionSpec() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithOr", IfaceWithOr.class), 0);
        
        assertTrue(resolver.supportsParameter(param));
        assertThat(resolver.resolveArgument(param, null, req, null)).isInstanceOf(IfaceWithOr.class);
	}
	
	@Test
	public void resolvedSpecHasWorkingToStringMethod() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithOr", IfaceWithOr.class), 0);
        
		Object resolved = resolver.resolveArgument(param, null, req, null);
		
		assertThat(resolved.toString()).startsWith("IfaceWithOr[");
	}
	
	@Test
	public void createsConjunctionOutOfSpecsFromWholeInheritanceTree() throws Exception {
		MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("methodWithInheritanceTree", GrandChildInterface.class), 0);
        
		Object resolved = resolver.resolveArgument(param, null, req, null);
		
		net.kaczmarzyk.spring.data.jpa.domain.Conjunction<Object> resolvedConjunction = ReflectionUtils.get(ReflectionUtils.get(resolved, "CGLIB$CALLBACK_0"), "val$targetSpec");
		Collection<Specification<Object>> resolvedInnerSpecs = ReflectionUtils.get(resolvedConjunction, "innerSpecs");
		assertThat(resolvedInnerSpecs)
			.hasSize(4)
			.containsOnly(
				new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "deleted", new String[] { "false" }, converter)),
				new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
						new Like<>(queryCtx, "name", new String[] { "Homer" }),
						new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "weight", new String[] { "121" }, converter))),
				new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
						new Like<>(queryCtx, "name2", new String[] { "Max" }),
						new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "weight2", new String[] { "99" }, converter))),
				new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
						new EmptyResultOnTypeMismatch<>(new Equal<>(queryCtx, "gold", new String[] { "true" }, converter)),
						new EmptyResultOnTypeMismatch<>(new In<>(queryCtx, "status", new String [] { "ACTIVE", "VERY_ACTIVE" }, converter))));
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
