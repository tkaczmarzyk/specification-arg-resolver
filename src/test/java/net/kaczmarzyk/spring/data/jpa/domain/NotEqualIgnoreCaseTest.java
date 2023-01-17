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
package net.kaczmarzyk.spring.data.jpa.domain;

import com.jparams.verifier.tostring.ToStringVerifier;
import net.kaczmarzyk.spring.data.jpa.Customer;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * @author Mateusz Fedkowicz
 **/
public class NotEqualIgnoreCaseTest extends NotEqualTest {

	private static final String INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE = "Invalid size of 'httpParamValues' array, Expected 1 but was ";

	@Test
	public void filtersByStringCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notSimpson = notEqualIgnoreCaseSpec("lastName", "SIMpsOn");
		assertFilterMembers(notSimpson, joeQuimby);

		NotEqualIgnoreCase<Customer> notHomer = notEqualIgnoreCaseSpec("firstName", "HoMeR");
		assertFilterMembers(notHomer, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByEnumCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notMale = notEqualIgnoreCaseSpec("gender", "maLe");
		assertFilterMembers(notMale, margeSimpson);

		NotEqualIgnoreCase<Customer> notFemale = notEqualIgnoreCaseSpec("gender", "fEmALE");
		assertFilterMembers(notFemale, homerSimpson);
	}


	@Test
	public void filtersByCharPrimitiveCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notFemale = new NotEqualIgnoreCase<>(queryCtx, "genderAsChar", new String[] { "f" }, defaultConverter);
		assertFilterMembers(notFemale, homerSimpson, joeQuimby);

		NotEqualIgnoreCase<Customer> notMale = new NotEqualIgnoreCase<>(queryCtx, "genderAsChar", new String[] { "m" }, defaultConverter);
		assertFilterMembers(notMale, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByCharacterCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notFemale = new NotEqualIgnoreCase<>(queryCtx, "genderAsCharacter", new String[] { "f" }, defaultConverter);
		assertFilterMembers(notFemale, homerSimpson);

		NotEqualIgnoreCase<Customer> notMale = new NotEqualIgnoreCase<>(queryCtx, "genderAsCharacter", new String[] { "m" }, defaultConverter);
		assertFilterMembers(notMale, margeSimpson);
	}

	@Test
	public void rejectsNullArgumentArray() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqualIgnoreCase<>(queryCtx, "path", null, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "null");
	}

	@Test
	public void rejectsMissingArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqualIgnoreCase<>(queryCtx, "path", new String[] {}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[]");
	}

	@Test
	public void rejectsTooManyArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new NotEqualIgnoreCase<>(queryCtx, "path", new String[] {"2014-03-10", "2014-03-11"}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[2014-03-10, 2014-03-11]");
	}

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(NotEqualIgnoreCase.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}

	@Test
	public void toStringVerifier() {
		ToStringVerifier.forClass(NotEqualIgnoreCase.class)
				.withIgnoredFields("queryContext")
				.verify();
	}

	private <T> NotEqualIgnoreCase<T> notEqualIgnoreCaseSpec(String path, Object expectedValue) {
		NotEqualIgnoreCase<T> spec = new NotEqualIgnoreCase<>(queryCtx, path, new String[]{expectedValue.toString()}, defaultConverter);
		spec.setLocale(Locale.getDefault());
		return spec;
	}

}
