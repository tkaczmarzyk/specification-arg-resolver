package net.kaczmarzyk.spring.data.jpa.web;

import com.jparams.verifier.tostring.ToStringVerifier;
import org.junit.Test;

public class DefaultQueryContextTest {

	@Test
	public void toStringVerifier() {
		ToStringVerifier.forClass(DefaultQueryContext.class)
				.verify();
	}

}