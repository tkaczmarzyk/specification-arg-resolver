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
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecificationArgumentResolverAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void contextContainsDefaultBeansIfThereAreNotAnyOther() {
        this.contextRunner
                .withUserConfiguration(SpecificationArgumentResolverAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SpecificationArgumentResolver.class);
                    assertThat(context).hasBean("defaultSpecificationArgumentResolver");

                    assertThat(context).hasSingleBean(PageableHandlerMethodArgumentResolver.class);
                    assertThat(context).hasBean("defaultPageableHandlerMethodArgumentResolver");
                });
    }

    @Test
    public void contextContainsBeanForSpringdocOperationCustomizationWhenPropertyIsSet() {
        this.contextRunner
                .withPropertyValues("specification-arg-resolver.spring-doc-operation-customizer=true")
                .withUserConfiguration(SpecificationArgumentResolverAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SpecificationArgResolverSpringdocOperationCustomizer.class);
                    assertThat(context).hasBean("defaultSpecificationArgResolverSpringdocOperationCustomizer");
                });
    }

    @Test
    public void contextDoesNotContainsBeanForSpringdocOperationCustomizationWhenPropertyIsNotSet() {
        this.contextRunner
                .withUserConfiguration(SpecificationArgumentResolverAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SpecificationArgResolverSpringdocOperationCustomizer.class);
                });
    }

    @Test
    public void contextDoesNotContainsDefaultBeansIfThereAreOtherDeclared() {
        this.contextRunner
                .withPropertyValues("specification-arg-resolver.spring-doc-operation-customizer=true")
                .withUserConfiguration(SarTestConfiguration.class, SpecificationArgumentResolverAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SpecificationArgumentResolver.class);
                    assertThat(context).hasBean("differentSpecificationArgumentResolver");

                    assertThat(context).hasSingleBean(PageableHandlerMethodArgumentResolver.class);
                    assertThat(context).hasBean("differentPageableHandlerMethodArgumentResolver");

                    assertThat(context).hasSingleBean(SpecificationArgResolverSpringdocOperationCustomizer.class);
                    assertThat(context).hasBean("differentSpecificationArgResolverSpringdocOperationCustomizer");
                });
    }
}
