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
package net.kaczmarzyk.autoconfiguration;

import net.kaczmarzyk.spring.data.jpa.swagger.springdoc.SpecificationArgResolverSpringdocOperationCustomizer;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.Locale;

@AutoConfiguration
public class SpecificationArgumentResolverAutoConfiguration {

    @Autowired
    AbstractApplicationContext applicationContext;

    @Value(value = "${specification-arg-resolver.locale:EN}")
    String locale;

    @ConditionalOnMissingBean
    @Bean
    public SpecificationArgumentResolver defaultSpecificationArgumentResolver() {
        return new SpecificationArgumentResolver(applicationContext, Locale.forLanguageTag(locale));
    }

    @ConditionalOnMissingBean
    @Bean
    public PageableHandlerMethodArgumentResolver defaultPageableHandlerMethodArgumentResolver() {
        return new PageableHandlerMethodArgumentResolver();
    }

    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "specification-arg-resolver", name = "spring-doc-operation-customizer", havingValue = "true")
    @Bean
    public SpecificationArgResolverSpringdocOperationCustomizer defaultSpecificationArgResolverSpringdocOperationCustomizer() {
        return new SpecificationArgResolverSpringdocOperationCustomizer();
    }
}
