package net.kaczmarzyk.autoconfiguration;

import net.kaczmarzyk.spring.data.jpa.swagger.springdoc.SpecificationArgResolverSpringdocOperationCustomizer;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

@Configuration
public class SarTestConfiguration {

    @Bean
    public SpecificationArgumentResolver differentSpecificationArgumentResolver() {
        return new SpecificationArgumentResolver();
    }

    @Bean
    public PageableHandlerMethodArgumentResolver differentPageableHandlerMethodArgumentResolver() {
        return new PageableHandlerMethodArgumentResolver();
    }

    @Bean
    public SpecificationArgResolverSpringdocOperationCustomizer differentSpecificationArgResolverSpringdocOperationCustomizer() {
        return new SpecificationArgResolverSpringdocOperationCustomizer();
    }
}
