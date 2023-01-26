/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.benchmark.argument.web;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.utils.JsonBodyParams;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationFactory;
import net.kaczmarzyk.spring.data.jpa.web.WebRequestProcessingContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Executable;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * This class measures the average time of resolving specification using {@code SpecificationFactory} for various types of passing arguments (params, pathVars, headers, jsonPaths).
 * Each benchmark is passing three arguments and uses interface with exact three specifications.
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class WebProcessingContextArgumentsBenchmark {

	private static final SpecificationFactory SPECIFICATION_FACTORY = new SpecificationFactory(null, null, Locale.getDefault());

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureCreatingSpecWithMultipleParamsUsingWebProcessingContext(Blackhole blackhole) {

		PerformanceTestsMockWebRequest request = new PerformanceTestsMockWebRequest("/params");
		request.addParameterValues("age", singletonList("19"));
		request.addParameterValues("city", singletonList("Springfield"));
		request.addParameterValues("criminalPast", singletonList("false"));

		MethodParameter methodParameter = testMethodParameter("multipleParams", MultipleParamsSpecification.class);
		WebRequestProcessingContext context = new WebRequestProcessingContext(methodParameter, request);

		Specification<?> specification = SPECIFICATION_FACTORY.createSpecificationDependingOn(context);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureCreatingSpecWithMultiplePathVarsUsingWebProcessingContext(Blackhole blackhole) {

		PerformanceTestsMockWebRequest request = new PerformanceTestsMockWebRequest("/pathVariables/19/Springfield/false");
		Map<String, String> pathVariables = Map.of(
			"age", "19",
			"city", "Springfield",
			"criminalPast", "false"
		);
		request.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables, SCOPE_REQUEST);

		MethodParameter methodParameter = testMethodParameter("multiplePathVars", MultiplePathVarsSpecification.class);
		WebRequestProcessingContext context = new WebRequestProcessingContext(methodParameter, request);

		Specification<?> specification = SPECIFICATION_FACTORY.createSpecificationDependingOn(context);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureCreatingSpecWithMultipleHeadersUsingWebProcessingContext(Blackhole blackhole) {

		PerformanceTestsMockWebRequest request = new PerformanceTestsMockWebRequest("/headers");
		request.addHeaderValues("age", singletonList("19"));
		request.addHeaderValues("city", singletonList("Springfield"));
		request.addHeaderValues("criminalPast", singletonList("false"));

		MethodParameter methodParameter = testMethodParameter("multipleHeaders", MultipleHeadersSpecification.class);
		WebRequestProcessingContext context = new WebRequestProcessingContext(methodParameter, request);

		Specification<?> specification = SPECIFICATION_FACTORY.createSpecificationDependingOn(context);
		blackhole.consume(specification);
	}

	@Benchmark
	@BenchmarkMode(AverageTime)
	@OutputTimeUnit(NANOSECONDS)
	@Warmup(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Measurement(time = 1000, timeUnit = MILLISECONDS, iterations = 5)
	@Fork(3)
	public void measureCreatingSpecWithMultipleJsonPathsUsingWebProcessingContext(Blackhole blackhole) {

		PerformanceTestsMockWebRequest request = new PerformanceTestsMockWebRequest("/jsonPaths");
		MethodParameter methodParameter = testMethodParameter("multipleJsonPaths", MultipleJsonPathsSpecification.class);

		String jsonBody = "{ \"age\": \"\", \"city\": \"Springfield\", \"criminalPast\": \"false\" }";
		WebRequestProcessingContext context = new WebRequestProcessingContext(methodParameter, request);
		ReflectionTestUtils.setField(context, "bodyParams", JsonBodyParams.parse(jsonBody));

		Specification<?> specification = SPECIFICATION_FACTORY.createSpecificationDependingOn(context);
		blackhole.consume(specification);
	}

	private MethodParameter testMethodParameter(String methodName, Class<?> specificationType) {
		return MethodParameter.forExecutable(testMethod(methodName, specificationType), 0);
	}

	private Executable testMethod(String methodName, Class<?> specClass) {
		try {
			return BenchmarkController.class.getMethod(methodName, specClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Or({
		@Spec(path = "age", params = "age", spec = Equal.class),
		@Spec(path = "city", params = "city", spec = Equal.class),
		@Spec(path = "criminalPast", params = "criminalPast", spec = Equal.class),
	})
	private interface MultipleParamsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", pathVars = "age", spec = Equal.class),
		@Spec(path = "city", pathVars = "city", spec = Equal.class),
		@Spec(path = "criminalPast", pathVars = "criminalPast", spec = Equal.class),
	})
	private interface MultiplePathVarsSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", headers = "age", spec = Equal.class),
		@Spec(path = "city", headers = "city", spec = Equal.class),
		@Spec(path = "criminalPast", headers = "criminalPast", spec = Equal.class),
	})
	private interface MultipleHeadersSpecification extends Specification<Object> {
	}

	@Or({
		@Spec(path = "age", jsonPaths = "age", spec = Equal.class),
		@Spec(path = "city", jsonPaths = "city", spec = Equal.class),
		@Spec(path = "criminalPast", jsonPaths = "criminalPast", spec = Equal.class),
	})
	private interface MultipleJsonPathsSpecification extends Specification<Object> {
	}

	private static class BenchmarkController {

		@RequestMapping(value = "/params")
		public void multipleParams(MultipleParamsSpecification spec) {
		}

		@RequestMapping(value = "/pathVars/{age}/{city}/{criminalPast}")
		public void multiplePathVars(MultiplePathVarsSpecification spec) {
		}

		@RequestMapping(value = "/headers")
		public void multipleHeaders(MultipleHeadersSpecification spec) {
		}

		@RequestMapping(value = "/jsonPaths")
		public void multipleJsonPaths(MultipleJsonPathsSpecification spec) {
		}
	}
}
