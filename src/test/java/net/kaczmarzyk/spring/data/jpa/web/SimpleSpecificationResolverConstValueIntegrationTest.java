package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.IntegrationTestBaseWithSARConfiguredWithApplicationContext;
import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Jakub Radlica
 */
public class SimpleSpecificationResolverConstValueIntegrationTest extends IntegrationTestBaseWithSARConfiguredWithApplicationContext {
	
	private Converter defaultConverter = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Autowired
	AbstractApplicationContext abstractApplicationContext;
	
	
	@Test
	public void returnsSpecificationWithConstValueInSpEL() {
		SimpleSpecificationResolver resolver = new SimpleSpecificationResolver(null, abstractApplicationContext);
		
		MethodParameter param = methodParameter("testMethodWithConstValueInSpEL", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));
		
		assertThat(resolved)
				.isEqualTo(equalWithPathAndExpectedValue(ctx, "thePath", "Property value"));
	}
	
	@Test
	public void throwsIllegalArgumentExceptionWhenTryingToResolveConstValueInInvalidSpELSyntax() {
		SimpleSpecificationResolver resolver = new SimpleSpecificationResolver(null, abstractApplicationContext);
		
		MethodParameter param = methodParameter("testMethodWithConstValueInInvalidSpELSyntax", Specification.class);
		NativeWebRequest req = mock(NativeWebRequest.class);
		
		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);
		
		ThrowableAssertions.assertThrows(
				IllegalArgumentException.class,
				() -> resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class)),
				"Invalid SpEL expression: '#{${SpEL-support.constVal.value}.concat('test')}'"
		);
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
	
	 private static class TestController {
		
		 public void testMethodWithConstValue(
				 @Spec(path = "thePath", spec = Equal.class, constVal = "test") Specification<Object> spec) {
		 }
		 
		public void testMethodWithConstValueInSpEL(
				@Spec(path = "thePath", spec = Equal.class, constVal = "#{'${SpEL-support.constVal.value}'.concat('ue')}") Specification<Object> spec) {
		}
		
		 public void testMethodWithConstValueInInvalidSpELSyntax(
				 @Spec(path = "thePath", spec = Equal.class, constVal = "#{${SpEL-support.constVal.value}.concat('test')}") Specification<Object> spec) {
		 }
	}
	
	protected MethodParameter methodParameter(String methodName, Class<?> specClass) {
		try {
			return MethodParameter.forExecutable(
					TestController.class.getMethod(methodName, specClass),
					0
			);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
