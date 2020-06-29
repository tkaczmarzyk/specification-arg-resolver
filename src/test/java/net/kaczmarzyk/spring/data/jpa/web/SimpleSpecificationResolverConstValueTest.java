package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SimpleSpecificationResolverConstValueTest extends ResolverTestBase {
	
	public SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();
	
	@Test
	public void returnsSpecificationWithConstValue() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConstValue"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "constValue"));
	}
	
	@Test
	public void returnsSpecificationWithRawSpELConstValueIfSpecificationArgumentResolverIsMisconfigured() {
		MethodParameter param = MethodParameter.forExecutable(testMethod("testMethodWithConstValueInSpEL"), 0);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "#{'Te'.concat('st')}"));
	}
	
	private EmptyResultOnTypeMismatch<?> equalWithPathAndExpectedValue(WebRequestProcessingContext ctx, String path, String expectedValue) {
		return new EmptyResultOnTypeMismatch<>(
				new Equal<>(
						ctx.queryContext(),
						path,
						new String[]{ expectedValue },
						defaultConverter
				)
		);
	}
	
	public static class TestController {
		
		public void testMethodWithConstValue(@Spec(path = "thePath", spec = Equal.class, constVal = "constValue") Specification<Object> spec) {
		}
		
		public void testMethodWithConstValueInSpEL(@Spec(path = "thePath", spec = Equal.class, constVal = "#{'Te'.concat('st')}") Specification<Object> spec) {
		}
	}
	
	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
