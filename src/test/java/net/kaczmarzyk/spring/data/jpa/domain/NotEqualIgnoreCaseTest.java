package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import org.junit.Test;

/**
 * @author Mateusz Fedkowicz
 **/
public class NotEqualIgnoreCaseTest extends NotEqualTest {

	@Test
	public void filtersByStringCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notSimpson = notEqualIgnoreCaseSpec("lastName", "SIMpsOn");
		assertFilterMembers(notSimpson, joeQuimby);

		NotEqualIgnoreCase<Customer> notHomer = notEqualIgnoreCaseSpec("firstName", "HoMeR");
		assertFilterMembers(notHomer, margeSimpson, joeQuimby);
	}

	private <T> NotEqualIgnoreCase<T> notEqualIgnoreCaseSpec(String path, Object expectedValue) {
		return new NotEqualIgnoreCase<>(queryCtx, path, new String[]{expectedValue.toString()}, defaultConverter);
	}

}
