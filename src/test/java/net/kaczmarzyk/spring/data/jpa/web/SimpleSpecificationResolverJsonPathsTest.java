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
package net.kaczmarzyk.spring.data.jpa.web;

import com.google.gson.JsonParseException;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.JsonBodyParams;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.NativeWebRequest;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.util.Locale;

public class SimpleSpecificationResolverJsonPathsTest extends ResolverTestBase {

	private static final Converter defaultConverter = Converter.withTypeMismatchBehaviour(EXCEPTION, null, Locale.getDefault());
	private static final SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

	@Test
	public void buildsSpecificationWithJsonPath() {
		//given
		MethodParameter param = methodParameter("testMethodWithSingleJsonPath", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		String json = "{ \"customerId\": \"customerValue\" }";

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		ReflectionTestUtils.setField(ctx, "bodyParams", JsonBodyParams.parse(json));

		//when
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		//then
		assertThat(resolved)
				.isEqualTo(new Like<>(ctx.queryContext(), "thePath", "customerValue"));
	}

	@Test
	public void buildsSpecificationWithMultipleJsonPaths() {
		//given
		MethodParameter param = methodParameter("testMethodWithMultipleJsonPaths", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		String json = "{ \"date\": { \"dateAfter\": \"valueAfter\", \"dateBefore\": \"valueBefore\"}}";

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		ReflectionTestUtils.setField(ctx, "bodyParams", JsonBodyParams.parse(json));

		//when
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		//then
		assertThat(resolved)
				.isEqualTo(new Between<>(ctx.queryContext(), "thePath", new String[]{"valueAfter", "valueBefore"}, defaultConverter));
	}

	@Test
	public void buildsSpecificationWithJsonPathPointingToArrayOfPrimitiveTypes() {
		//given
		MethodParameter param = methodParameter("testMethodWithSingleJsonPathPointingToArrayOfPrimitiveTypes", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		String json = "{ \"customer\": { \"gender\": [\"MALE\", \"FEMALE\"]}}";

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		ReflectionTestUtils.setField(ctx, "bodyParams", JsonBodyParams.parse(json));

		//when
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

		//then
		assertThat(resolved)
				.isEqualTo(new In<>(ctx.queryContext(), "thePath", new String[]{"MALE", "FEMALE"}, defaultConverter));
	}

	@Test
	public void throwsIllegalArgumentExceptionBuildingSpecificationWithJsonPathPointingToArrayOfNonPrimitiveTypes() {
		//given
		MethodParameter param = methodParameter("testMethodWithSingleJsonPathPointingToArrayOfNonPrimitiveTypes", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		String json = "{ \"workshopInfo\": { \"customerTypes\": [{ \"type1\": \"value1\" }, { \"type2\": \"value2\" }]}}";

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		ReflectionTestUtils.setField(ctx, "bodyParams", JsonBodyParams.parse(json));

		//when-then
		try {
			resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
			fail("expected exception");
		} catch (IllegalArgumentException exception) {
			assertThat(exception)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Array by key contains not primitives");
		}
	}

	@Test
	public void throwsJsonParseExceptionBuildingSpecificationWithJsonPathContainingArrayOfNonPrimitiveTypes() {
		//given
		MethodParameter param = methodParameter("testMethodWithSingleJsonPathContainingArrayOfNonPrimitiveTypes", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		String json = "{ \"workshopInfo\": { \"customerTypes\": [{ \"type1\": \"value1\" }, { \"type2\": \"value2\" }]}}";

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		ReflectionTestUtils.setField(ctx, "bodyParams", JsonBodyParams.parse(json));

		//when-then
		try {
			resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
			fail("expected exception");
		} catch (JsonParseException exception) {
			assertThat(exception)
				.isInstanceOf(JsonParseException.class)
				.hasMessageContaining("Failed parse JSON node with key type1. Should be JSON object");
		}
	}


	public static class TestController {

		public void testMethodWithSingleJsonPath(@Spec(path = "thePath", jsonPaths="customerId", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void testMethodWithMultipleJsonPaths(@Spec(path = "thePath", jsonPaths={"date.dateAfter", "date.dateBefore"}, spec = Between.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void testMethodWithSingleJsonPathPointingToArrayOfPrimitiveTypes(@Spec(path = "thePath", jsonPaths="customer.gender", spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void testMethodWithSingleJsonPathContainingArrayOfNonPrimitiveTypes(@Spec(path = "thePath", jsonPaths="workshopInfo.customerTypes.type1", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}

		public void testMethodWithSingleJsonPathPointingToArrayOfNonPrimitiveTypes(@Spec(path = "thePath", jsonPaths="workshopInfo.customerTypes", spec = In.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
		}
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
