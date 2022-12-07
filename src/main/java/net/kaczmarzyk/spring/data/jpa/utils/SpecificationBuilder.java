/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import net.kaczmarzyk.spring.data.jpa.web.ProcessingContext;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationFactory;
import net.kaczmarzyk.spring.data.jpa.web.StandaloneProcessingContext;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

/**
 * SpecificationBuilder allows creating specification apart from web layer.
 * It is recommended to use builder methods that corresponding to the type of argument passed to sepcification.
 * <ul>
 * <li> {@code params = <args> => withParams(<argName>, <values...>)}, single param argument can provide multiple values </li>
 * <li> {@code pathVars = <args> => withPathVar(<argName>, <value>)}, single pathVar argument can provide single value </li>
 * <li> {@code headers = <args> => withHeader(<argName>, <value>)}, single header argument can provide single value </li>
 * </ul>
 * 
 * @author Jakub Radlica
 * @author Kacper Leśniak (Tratif sp. z o.o.)
 */
public class SpecificationBuilder<T extends Specification> {

	private SpecificationFactory specificationFactory = new SpecificationFactory(null, null);

	private Class<T> specInterface;

	private Map<String, String[]> fallbackSpecificationParamValues = new HashMap<>();
	private Map<String, String> pathVars = new HashMap<>();
	private Map<String, String[]> params = new HashMap<>();
	private Map<String, String> headers = new HashMap<>();

	private SpecificationBuilder(Class<T> specInterface) {
		this.specInterface = specInterface;
	}

	public static <T extends Specification<?>> SpecificationBuilder<T> specification(Class<T> specInterface) {
		return new SpecificationBuilder<T>(specInterface);
	}

	public SpecificationBuilder<T> withSpecificationFactory(SpecificationFactory specificationFactory) {
		this.specificationFactory = specificationFactory;
		return this;
	}

	/**
	 * The direct methods for params/headers/pathVars should be used in most common scenarios in order to avoid
	 * hard-to-debug errors. Use this method only if you really understand how it works.
	 */
	@SuppressWarnings("unckecked")
	public SpecificationBuilder<T> withArg(String arg, String... values) {
		this.fallbackSpecificationParamValues.put(arg, values);
		return this;
	}

	public SpecificationBuilder<T> withParam(String param, String... values) {
		this.params.put(param, values);
		return this;
	}

	public SpecificationBuilder<T> withPathVar(String pathVar, String value) {
		this.pathVars.put(pathVar, value);
		return this;
	}

	public SpecificationBuilder<T> withHeader(String header, String value) {
		this.headers.put(header, value);
		return this;
	}

	public T build() {
		ProcessingContext context = createContext();
		return (T) specificationFactory.createSpecificationDependingOn(context);
	}

	private ProcessingContext createContext() {
		return new StandaloneProcessingContext(specInterface, fallbackSpecificationParamValues, pathVars, params, headers);
	}

}
