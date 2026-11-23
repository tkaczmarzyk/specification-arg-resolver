/*
 * Copyright 2014-2026 the original author or authors.
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
package net.kaczmarzyk;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.swagger.springdoc.SpecificationArgResolverSpringdocOperationCustomizer;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SwaggerDocumentationE2eTest extends E2eTestBase {

	private static final String API_DOCS_GROUP = "swagger-e2e";

	private static final String EXPECTED_DOCUMENTATION = "swagger/expected-api-docs.yaml";

	@Configuration
	public static class SwaggerConfig {

		@Bean
		public GroupedOpenApi swaggerE2eApi() {
			return GroupedOpenApi.builder()
				.group(API_DOCS_GROUP)
				.pathsToMatch("/swagger-e2e/**")
				.addOperationCustomizer(new SpecificationArgResolverSpringdocOperationCustomizer())
				.build();
		}
	}

	@Controller
	public static class TestController {

		@Autowired
		CustomerRepository customerRepo;

		@GetMapping(value = "/swagger-e2e/customers/{customerId}", params = "lastName")
		@ResponseBody
		public Object findCustomers(
				@And({
						@Spec(path = "id", pathVars = "customerId", spec = Equal.class, description = "Identifier of the customer"),
						@Spec(path = "lastName", params = "lastName", spec = Like.class, description = "Fragment of the customer's last name"),
						@Spec(path = "registrationDate", params = { "registeredAfter", "registeredBefore" }, spec = Between.class, description = "Registration date range boundary"),
						@Spec(path = "gender", headers = "gender", spec = Equal.class, description = "Gender of the customer"),
						@Spec(path = "firstName", params = "firstName", spec = Like.class)
				}) Specification<Customer> spec) {

			return customerRepo.findAll(spec);
		}
	}

	@Test
	public void generatesExpectedDocumentationForControllerUsingSpecAnnotations() throws Exception {
		String generatedDocumentation = mockMvc.perform(get("/v3/api-docs.yaml/" + API_DOCS_GROUP))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString(UTF_8);

		assertThat(parseYaml(generatedDocumentation))
			.isEqualTo(parseYaml(expectedDocumentation()));
	}

	private String expectedDocumentation() throws IOException {
		return new ClassPathResource(EXPECTED_DOCUMENTATION).getContentAsString(UTF_8);
	}

	// compare parsed documents, so formatting and key order do not matter
	private Object parseYaml(String yaml) {
		return new Yaml().load(yaml);
	}
}
