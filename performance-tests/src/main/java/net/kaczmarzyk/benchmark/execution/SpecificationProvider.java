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
package net.kaczmarzyk.benchmark.execution;

import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.benchmark.model.Customer;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.IsMember;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

import static java.util.Map.entry;
import static net.kaczmarzyk.spring.data.jpa.utils.SpecificationBuilder.specification;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class SpecificationProvider {

	public static final Map<String, Specification<Customer>> EQUAL_SPECIFICATIONS = Map.ofEntries(
		entry("equalStringSpec", specification(EqualStringSpec.class).withParam("firstName", "firstName").build()),
		entry("equalLocalDateTimeSpec", specification(EqualLocalDateTimeSpec.class).withParam("lastOrderTime", "2016-10-17T18:29:00").build()),
		entry("equalLocalDateSpec", specification(EqualLocalDateSpec.class).withParam("birthDate", "2023-01-20").build()),
		entry("equalDateSpec", specification(EqualDateSpec.class).withParam("registrationDate", "2023-01-20").build()),
		entry("equalCalendarSpec", specification(EqualCalendarSpec.class).withParam("registrationDate", "2023-01-20").build()),
		entry("equalOffsetDateTimeSpec", specification(EqualOffsetDateTimeSpec.class).withParam("dateOfNextSpecialOffer", "2020-07-16T16:17:00.000+00:00").build()),
		entry("equalInstantSpec", specification(EqualInstantSpec.class).withParam("dateOfNextSpecialOfferInstant", "2020-07-16T16:17:00.000+04:00").build()),
		entry("equalTimestampSpec", specification(EqualTimestampSpec.class).withParam("lastSeen", "2022-10-12T22:17:13.000Z").build()),
		entry("equalEnumSpec", specification(EqualEnumSpec.class).withParam("gender", "MALE").build()),
		entry("equalBooleanPrimitiveTypeSpec", specification(EqualBooleanPrimitiveTypeSpec.class).withParam("gold", "true").build()),
		entry("equalBooleanObjectSpec", specification(EqualBooleanObjectSpec.class).withParam("goldObj", "true").build()),
		entry("equalIntPrimitiveTypeSpec", specification(EqualIntPrimitiveTypeSpec.class).withParam("weightInt", "50").build()),
		entry("equalIntegerObjectSpec", specification(EqualIntegerObjectSpec.class).withParam("weight", "50").build()),
		entry("equalLongPrimitiveTypeSpec", specification(EqualLongPrimitiveTypeSpec.class).withParam("weightLong", "50").build()),
		entry("equalLongObjectSpec", specification(EqualLongObjectSpec.class).withParam("id", "50").build()),
		entry("equalFloatPrimitiveTypeSpec", specification(EqualFloatPrimitiveTypeSpec.class).withParam("weightFloat", "50.0").build()),
		entry("equalDoubleObjectSpec", specification(EqualDoubleObjectSpec.class).withParam("weightDouble", "50.0").build()),
		entry("equalBigDecimalSpec", specification(EqualBigDecimalSpec.class).withParam("weightBigDecimal", "50.0").build()),
		entry("equalUUIDSpec", specification(EqualUUIDSpec.class).withParam("refCode", "2cdf7f82-2e32-4219-be0c-a5457e79c7b1").build()),
		entry("equalCharPrimitiveTypeSpec", specification(EqualCharPrimitiveTypeSpec.class).withParam("genderAsChar", "a").build()),
		entry("equalCharacterObjectSpec", specification(EqualCharacterObjectSpec.class).withParam("genderAsCharacter", "a").build())
	);

	public static Map<String, Specification<Customer>> JOIN_SPECIFICATIONS = Map.ofEntries(
		entry("joinSpec", specification(JoinSpec.class).withParam("o.itemName", "book").build()),
		entry("joinFetchSpec", specification(JoinFetchSpec.class).withParam("o.itemName", "book").build())
	);

	public static Map<String, Specification<Customer>> STRING_VALUES_SPECIFICATIONS = Map.ofEntries(
		entry("inStringSpec", specification(InSpec.class).withParam("firstName", "Joe").build()),
		entry("isMemberStringSpec", specification(IsMemberSpec.class).withParam("phoneNumber", "123").build()),
		entry("likeStringSpec", specification(LikeSpec.class).withParam("firstName", "Joe").build())
	);

	@Spec(path="firstName", params = "firstName", spec= Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualStringSpec extends Specification<Customer> {
	}

	@Spec(path="lastOrderTime", params = "lastOrderTime", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualLocalDateTimeSpec extends Specification<Customer> {
	}

	@Spec(path="birthDate", params = "birthDate", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualLocalDateSpec extends Specification<Customer> {
	}

	@Spec(path="registrationDate", params = "registrationDate", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualDateSpec extends Specification<Customer> {
	}

	@Spec(path="registrationDate", params = "registrationDate", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualCalendarSpec extends Specification<Customer> {
	}

	@Spec(path="dateOfNextSpecialOffer", params = "dateOfNextSpecialOffer", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualOffsetDateTimeSpec extends Specification<Customer> {
	}

	@Spec(path="dateOfNextSpecialOfferInstant", params = "dateOfNextSpecialOfferInstant", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualInstantSpec extends Specification<Customer> {
	}

	@Spec(path="lastSeen", params = "lastSeen", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualTimestampSpec extends Specification<Customer> {
	}

	@Spec(path="gender", params = "gender", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualEnumSpec extends Specification<Customer> {
	}

	@Spec(path="gold", params = "gold", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualBooleanPrimitiveTypeSpec extends Specification<Customer> {
	}

	@Spec(path="goldObj", params = "goldObj", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualBooleanObjectSpec extends Specification<Customer> {
	}

	@Spec(path="weightInt", params = "weightInt", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualIntPrimitiveTypeSpec extends Specification<Customer> {
	}

	@Spec(path="weight", params = "weight", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualIntegerObjectSpec extends Specification<Customer> {
	}

	@Spec(path="weightLong", params = "weightLong", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualLongPrimitiveTypeSpec extends Specification<Customer> {
	}

	@Spec(path="id", params = "id", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualLongObjectSpec extends Specification<Customer> {
	}

	@Spec(path="weightFloat", params = "weightFloat", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualFloatPrimitiveTypeSpec extends Specification<Customer> {
	}

	@Spec(path="weightDouble", params = "weightDouble", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualDoubleObjectSpec extends Specification<Customer> {
	}

	@Spec(path="weightBigDecimal", params = "weightBigDecimal", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualBigDecimalSpec extends Specification<Customer> {
	}

	@Spec(path="refCode", params = "refCode", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualUUIDSpec extends Specification<Customer> {
	}

	@Spec(path="genderAsChar", params = "genderAsChar", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualCharPrimitiveTypeSpec extends Specification<Customer> {
	}

	@Spec(path="genderAsCharacter", params = "genderAsCharacter", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface EqualCharacterObjectSpec extends Specification<Customer> {
	}

	@Join(path="orders", alias="o")
	@Spec(path="o.itemName", params = "o.itemName", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface JoinSpec extends Specification<Customer> {
	}

	@JoinFetch(paths="orders", alias="o", joinType= JoinType.LEFT)
	@Spec(path="o.itemName", params = "o.itemName", spec=Equal.class, onTypeMismatch = EXCEPTION)
	interface JoinFetchSpec extends Specification<Customer> {
	}

	@Spec(path="firstName", params = "firstName", spec= In.class, onTypeMismatch = EXCEPTION)
	interface InSpec extends Specification<Customer> {
	}

	@Spec(path="phoneNumbers", params = "phoneNumber", spec= IsMember.class, onTypeMismatch = EXCEPTION)
	interface IsMemberSpec extends Specification<Customer> {
	}

	@Spec(path="firstName", params = "firstName", spec= Like.class, onTypeMismatch = EXCEPTION)
	interface LikeSpec extends Specification<Customer> {
	}

}
