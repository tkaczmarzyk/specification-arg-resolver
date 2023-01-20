package net.kaczmarzyk.benchmark.specifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.jmx.support.RegistrationPolicy.IGNORE_EXISTING;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableMBeanExport(registration = IGNORE_EXISTING)
public class Application implements WebMvcConfigurer {

	@PersistenceContext
	public EntityManager entityManager;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new SpecificationArgumentResolver());
	}

	@Bean
	public EntityManager entityManager() {
		return entityManager;
	}
}
