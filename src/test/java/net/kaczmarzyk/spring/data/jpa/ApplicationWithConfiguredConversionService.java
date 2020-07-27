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
package net.kaczmarzyk.spring.data.jpa;

import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.jmx.support.RegistrationPolicy.IGNORE_EXISTING;

/**
 * @author Tomasz Kaczmarzyk
 */
@Configuration
@ComponentScan(basePackages = "net.kaczmarzyk", excludeFilters = {
		@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = Application.class),
		@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = ApplicationWithSARConfiguredWithApplicationContext.class)
})
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableMBeanExport(registration = IGNORE_EXISTING)
public class ApplicationWithConfiguredConversionService implements WebMvcConfigurer {
	
	@Autowired
	ConversionService conversionService;
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		SpecificationArgumentResolver specificationArgumentResolver = new SpecificationArgumentResolver(conversionService);
		argumentResolvers.add(specificationArgumentResolver);
	}
	
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToAddressConverter());
	}
	
	public static class StringToAddressConverter implements Converter<String, Address> {
		@Override
		public Address convert(String rawAddress) {
			Address address = new Address();
			address.setStreet(rawAddress);
			return address;
		}
	}
}
