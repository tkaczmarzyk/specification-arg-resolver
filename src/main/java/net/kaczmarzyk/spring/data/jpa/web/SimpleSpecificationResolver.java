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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.LocaleAware;
import net.kaczmarzyk.spring.data.jpa.domain.ZeroArgSpecification;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.expression.ParseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
class SimpleSpecificationResolver implements SpecificationResolver<Spec> {

    private final ConversionService conversionService;
    private final EmbeddedValueResolver embeddedValueResolver;
    private final Locale defaultLocale;
    
	public SimpleSpecificationResolver(ConversionService conversionService, AbstractApplicationContext applicationContext, Locale defaultLocale) {
		this.conversionService = conversionService;
		this.embeddedValueResolver = applicationContext != null ? new EmbeddedValueResolver(applicationContext.getBeanFactory()) : null;
		this.defaultLocale = defaultLocale;
	}
	
	public SimpleSpecificationResolver() {
		this(null, null, Locale.getDefault());
	}
	
	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return Spec.class;
	}
	
	public Specification<Object> buildSpecification(ProcessingContext context, Spec def) {
		try {
			Collection<String> args = resolveSpecArguments(context, def);
			if (args.isEmpty() && !isZeroArgSpec(def)) {
				return null;
			} else {
				String[] argsArray = args.toArray(new String[0]);
				Specification<Object> spec = newSpecification(def, argsArray, context);
				return def.onTypeMismatch().wrap(spec);
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Does the specification class expose at least one of the supported constructors?\n"
					+ "It can be either:\n"
					+ "  3-arg (QueryContext queryCtx, String path, String[] args)\n"
					+ "  4-arg (QueryContext queryCtx, String path, String[] args, Converter converter)\n"
					+ "  5-arg (QueryContext queryCtx, String path, String[] args, Converter converter, String[] config)", e);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private boolean isZeroArgSpec(Spec def) {
		return ZeroArgSpecification.class.isAssignableFrom(def.spec());
	}
	
	@SuppressWarnings("unchecked")
	private Specification<Object> newSpecification(Spec def, String[] argsArray, ProcessingContext context) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		
		QueryContext queryCtx = context.queryContext();
		Converter converter = resolveConverter(def);
		
		Specification<Object> spec;
		if (def.config().length == 0) {
			try {
				spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class)
						.newInstance(queryCtx, def.path(), argsArray);
			} catch (NoSuchMethodException e2) {
				spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class)
						.newInstance(queryCtx, def.path(), argsArray, converter);
			}
		} else {
			try {
				spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class, String[].class)
						.newInstance(queryCtx, def.path(), argsArray, converter, def.config());
			} catch (NoSuchMethodException e) {
				try {
					spec = def.spec().getConstructor(QueryContext.class, String.class, String[].class, Converter.class)
							.newInstance(queryCtx, def.path(), argsArray, converter);
				} catch (NoSuchMethodException e2) {
					// legacy constructor support, to retain backward-compatibility
					spec = def.spec().getConstructor(String.class, String[].class, String[].class)
							.newInstance(def.path(), argsArray, def.config());
				}
			}
		}
		
		if (spec instanceof LocaleAware) {
			Locale targetLocale = determineLocale(def);
			((LocaleAware) spec).setLocale(targetLocale);
		}
		
		return spec;
	}
	
	private Locale determineLocale(Spec def) {
		if (def.config().length == 0) {
			return defaultLocale;
		} else {
			return LocaleUtils.toLocale(def.config()[0]);
		}
	}

	private Converter resolveConverter(Spec def) {
		if (def.config().length == 0) {
			return Converter.withTypeMismatchBehaviour(def.onTypeMismatch(), conversionService, defaultLocale);
		}
		if (def.config().length == 1) {
			if (LocaleAware.class.isAssignableFrom(def.spec())) { // if specification is locale-aware, then we assume that config contains locale
				String localeConfig = def.config()[0];
				Locale customlocale = LocaleUtils.toLocale(localeConfig);
				return Converter.withTypeMismatchBehaviour(def.onTypeMismatch(), conversionService, customlocale);
			} else { // otherwise we assume that config contains date format
				String dateFormat = def.config()[0];
				return Converter.withDateFormat(dateFormat, def.onTypeMismatch(), conversionService);
			}
		}
		throw new IllegalStateException("config should contain only one value -- a date format"); // TODO support other config values as well
	}

	
	private Collection<String> resolveSpecArguments(ProcessingContext context, Spec specDef) {
		if (specDef.constVal().length != 0) {
			return resolveConstVal(specDef);
		} else if (specDef.pathVars().length != 0) {
			return resolveSpecArgumentsFromPathVariables(context, specDef);
		} else if (specDef.jsonPaths().length != 0) {
			return resolveSpecArgumentsFromBody(context, specDef);
		} else if (specDef.headers().length != 0) {
			return resolveSpecArgumentsFromRequestHeaders(context, specDef);
		} else {
			return resolveDefaultVal(context, specDef);
		}
	}

	private Collection<String> resolveConstVal(Spec specDef) {
		if (embeddedValueResolver != null && specDef.valueInSpEL()) {
			ArrayList<String> evaluatedArgs = new ArrayList<>(specDef.constVal().length);
			for (String rawConstVal : specDef.constVal()) {
				evaluatedArgs.add(evaluateRawSpELValue(rawConstVal));
			}
			return evaluatedArgs;
		} else {
			return asList(specDef.constVal());
		}
	}

	private Collection<String> resolveDefaultVal(ProcessingContext context, Spec specDef) {
		Collection<String> resolved = resolveSpecArgumentsFromHttpParameters(context, specDef);
		if (resolved.isEmpty() && specDef.defaultVal().length != 0) {
			if (embeddedValueResolver != null && specDef.valueInSpEL()) {
				for (String rawDefaultVal : specDef.defaultVal()) {
					resolved.add(evaluateRawSpELValue(rawDefaultVal));
				}
			} else {
				resolved.addAll(asList(specDef.defaultVal()));
			}
		}
		return resolved;
	}

	private String evaluateRawSpELValue(String rawSpELValue) {
		try {
			return embeddedValueResolver.resolveStringValue(rawSpELValue);
		} catch (BeansException|ParseException e) {
			throw new IllegalArgumentException("Invalid SpEL expression: '" + rawSpELValue + "'", e);
		}
	}
	
	private Collection<String> resolveSpecArgumentsFromPathVariables(ProcessingContext context, Spec specDef) {
		Collection<String> args = new ArrayList<>();
		for (String pathVar : specDef.pathVars()) {
			args.add(context.getPathVariableValue(pathVar));
		}
		return args;
	}

	private Collection<String> resolveSpecArgumentsFromBody(ProcessingContext context, Spec specDef) {
		return Arrays.stream(specDef.jsonPaths())
				.flatMap(param -> nullSafeArrayStream(context.getBodyParamValues(param)))
				.collect(toList());
	}

	private Collection<String> resolveSpecArgumentsFromRequestHeaders(ProcessingContext context, Spec specDef) {
		Collection<String> args = new ArrayList<>();
		for (String headerKey : specDef.headers()) {
			String headerValue = context.getRequestHeaderValue(headerKey);
			boolean isHeaderValueEmpty = headerValue == null || "".equals(headerValue);
			if (!isHeaderValueEmpty) {
				args.add(headerValue);
			}
		}
		return args;
	}
	
	private Collection<String> resolveSpecArgumentsFromHttpParameters(ProcessingContext context, Spec specDef) {
		Collection<String> args = new ArrayList<String>();

		DelimitationStrategy delimitationStrategy = DelimitationStrategy.of(specDef.paramSeparator());

		if (specDef.params().length != 0) {
			for (String webParamName : specDef.params()) {
				if (embeddedValueResolver != null && specDef.paramsInSpEL()) {
					webParamName = embeddedValueResolver.resolveStringValue(webParamName);
				}
				String[] parameterValues = context.getParameterValues(webParamName);
				if(parameterValues != null) {
					String[] httpParamValues = delimitationStrategy.extractSingularValues(parameterValues);
					addValuesToArgs(httpParamValues, args);
				}
			}
		} else {
			String[] parameterValues = context.getParameterValues(specDef.path());
			if(parameterValues != null) {
				String[] httpParamValues = delimitationStrategy.extractSingularValues(parameterValues);
				addValuesToArgs(httpParamValues, args);
			}
		}

		return args;
	}
	
	private void addValuesToArgs(String[] paramValues, Collection<String> args) {
		if (paramValues != null) {
			for (String paramValue : paramValues) {
				if (!StringUtils.isEmpty(paramValue)) {
					args.add(paramValue);
				}
			}
		}
	}

	private Stream<String> nullSafeArrayStream(String[] array) {
		return array != null ? Stream.of(array) : Stream.empty();
	}
	
	private static class DelimitationStrategy {
		
		public static final DelimitationStrategy NONE = new DelimitationStrategy("");
		
		private final String pattern;
		
		private DelimitationStrategy(String pattern) {
			this.pattern = pattern;
		}
		
		public static DelimitationStrategy of(char paramSeparator) {
			// 0 is a blank value of param separator
			if (paramSeparator == 0) {
				return DelimitationStrategy.NONE;
			}
			
			return new DelimitationStrategy(Pattern.quote(String.valueOf(paramSeparator)));
		}
		
		public String[] extractSingularValues(String[] args) {
			if (isEmpty()) {
				return args;
			}
			
			ArrayList<String> listOfSingularValues = new ArrayList<>();
			
			for (String arg : args) {
				listOfSingularValues.addAll(asList(arg.split(get())));
			}
			
			String[] singularValues = new String[listOfSingularValues.size()];
			
			return listOfSingularValues.toArray(singularValues);
		}
		
		public String get() {
			return pattern;
		}
		
		public boolean isEmpty() {
			return pattern.isEmpty();
		}
	}
	
}