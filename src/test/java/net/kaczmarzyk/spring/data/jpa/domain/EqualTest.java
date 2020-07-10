package net.kaczmarzyk.spring.data.jpa.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class EqualTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(Equal.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
}