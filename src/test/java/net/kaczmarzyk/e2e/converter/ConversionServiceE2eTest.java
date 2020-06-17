package net.kaczmarzyk.e2e.converter;

import net.kaczmarzyk.E2eTestBase;
import net.kaczmarzyk.spring.data.jpa.Address;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConversionServiceE2eTest extends E2eTestBase {
	
	@Controller
	@RequestMapping("/customerZ")
	public static class InstantSpecsController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping(params = "address")
		@ResponseBody
		public Object findCustomersWithDateOfNextSpecialOfferBefore_defaultInstantFormat(
				@Spec(path = "address", params = "address", spec = Equal.class) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
		
	}
	
	@Autowired
	List<HandlerMethodArgumentResolver> resolvers;

	@Test
	public void findsByAddressUsingConversionServiceWithCustomConverter() throws Exception {
		setupSpecificationArgumentResolverUsingConversionServiceWithCustomStringToAddressConverter();
		
		mockMvc.perform(get("/customerZ")
				.param("address", "Evergreen Terrace"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.firstName=='Homer')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Marge')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Bart')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Lisa')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Maggie')]").exists())
				.andExpect(jsonPath("$.[?(@.firstName=='Ned')]").exists())
				.andExpect(jsonPath("$[6]").doesNotExist());
	}
	
	public void setupSpecificationArgumentResolverUsingConversionServiceWithCustomStringToAddressConverter() {
		resolvers.removeIf(resolver -> resolver.getClass().isAssignableFrom(SpecificationArgumentResolver.class));
		
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new StringToAddressConverter());
		SpecificationArgumentResolver resolver = new SpecificationArgumentResolver(conversionService);
		
		resolvers.add(resolver);
	}
	
	public ConversionService conversionService() {
		DefaultConversionService defaultConversionService = new DefaultConversionService();
		defaultConversionService.addConverter(new StringToAddressConverter());
		return defaultConversionService;
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
