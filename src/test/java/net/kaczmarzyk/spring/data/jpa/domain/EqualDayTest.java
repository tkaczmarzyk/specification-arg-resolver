package net.kaczmarzyk.spring.data.jpa.domain;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class EqualDayTest {

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(EqualDay.class)
			.usingGetClass()
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

	@Test
	public void toStringVerifier() {
		ToStringVerifier.forClass(EqualDay.class)
			.withIgnoredFields("queryContext")
			.verify();
	}

}