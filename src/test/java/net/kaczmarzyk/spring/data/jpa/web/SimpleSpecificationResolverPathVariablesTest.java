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


import net.kaczmarzyk.spring.data.jpa.domain.DateBetween;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tomasz Kaczmarzyk
 */
public class SimpleSpecificationResolverPathVariablesTest extends ResolverTestBase {

    SimpleSpecificationResolver resolver = new SimpleSpecificationResolver();

    @Test
    public void throwsExceptionIfPathVariableNotPresent() {
    	 MethodParameter param = testMethodParameter("testMethodUsingNotExistingPathVariable");
         MockWebRequest req = new MockWebRequest("/customers/theCustomerIdValue/orders/theOrderIdValue");

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    assertThrows(InvalidPathVariableRequestedException.class, () -> resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class)));
    }

    @Test
    public void buildsTheSpecUsingPathVariableFromControllerClass() throws Exception {
        MethodParameter param = testMethodParameter("testMethodUsingPathVariableFromClass");
        MockWebRequest req = new MockWebRequest("/customers/theCustomerIdValue/orders/theOrderIdValue");

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Like<>(ctx.queryContext(), "thePath", new String[] { "theCustomerIdValue" }));
    }

    @Test
    public void buildsTheSpecUsingPathVariableFromControllerMethod() throws Exception {
        MethodParameter param = testMethodParameter("testMethodUsingPathVariableFromMethod");
        MockWebRequest req = new MockWebRequest("/customers/theCustomerIdValue/orders/theOrderIdValue");

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new Like<>(ctx.queryContext(), "thePath", new String[] { "theOrderIdValue" }));
    }

    @Test
    public void buildsTheSpecUsingMultiplePathVariables() throws Exception {
        MethodParameter param = testMethodParameter("testMethodUsingMultiplePathVariables");
        MockWebRequest req = new MockWebRequest("/customers/2019-01-25/orders/2019-01-27");

	    WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

	    Specification<?> resolved = resolver.buildSpecification(ctx, param.getParameterAnnotation(Spec.class));

        assertThat(resolved).isEqualTo(new DateBetween<>(ctx.queryContext(), "thePath", new String[] { "2019-01-25", "2019-01-27" }, defaultConverter));
    }

    @RequestMapping(path = "/customers/{customerId}")
    public static class TestController {

    	@RequestMapping(path = "/orders/{orderId}")
        public void testMethodUsingPathVariableFromClass(@Spec(path = "thePath", pathVars="customerId", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

    	@RequestMapping(path = "/orders/{orderId}")
        public void testMethodUsingPathVariableFromMethod(@Spec(path = "thePath", pathVars="orderId", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

    	@RequestMapping(path = "/orders/{orderId}")
        public void testMethodUsingMultiplePathVariables(
        			@Spec(path = "thePath", pathVars={"customerId", "orderId"}, spec = DateBetween.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {
        }

    	@RequestMapping(path = "/orders/{orderId}")
    	public void testMethodUsingNotExistingPathVariable(
    			@Spec(path = "thePath", pathVars="invoiceId", spec = Like.class, onTypeMismatch = EXCEPTION) Specification<Object> spec) {

    	}
    }

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
