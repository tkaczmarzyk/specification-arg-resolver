/*
 * Copyright 2014-2025 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.ComparableWithConverterTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


/**
 * Tests for the GreaterThan Specification.
 * 
 * @author TP Diffenbach
 */
public class GreaterThanTest extends ComparableWithConverterTestBase {
	
	@Override
    protected Specification<Customer> makeUUT(String path, String[] value, Converter converter) {
    	return new GreaterThan<Customer>(queryCtx, path, value, converter);
    }
    
    
    @Test
    public void filtersByEnumValue() {
        assertFilterContainsOnlyExpectedMembers("gender", "MALE", margeSimpson);
        assertFilterContainsOnlyExpectedMembers("gender", "FEMALE");
        assertFilterIsEmpty("gender", "OTHER");
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterContainsOnlyExpectedMembers("genderAsString", "MALE");
        assertFilterContainsOnlyExpectedMembers("genderAsString", "FEMALE", homerSimpson, moeSzyslak);
        assertFilterIsEmpty("genderAsString", "OTHER");
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterContainsOnlyExpectedMembers("id", moeSzyslak.getId().toString(), joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterContainsOnlyExpectedMembers("weightLong", HEAVIER_THAN_MARGE, moeSzyslak, homerSimpson);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterContainsOnlyExpectedMembers("weight", moeSzyslak.getWeight().toString(), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterContainsOnlyExpectedMembers("weightInt", String.valueOf(moeSzyslak.getWeightInt()), homerSimpson);
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterContainsOnlyExpectedMembers("weightDouble", HEAVIER_THAN_MOE_DOUBLE, homerSimpson);
    	assertFilterContainsOnlyExpectedMembers("weightDouble", moeSzyslak.getWeightDouble().toString(), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterContainsOnlyExpectedMembers("weightFloat", HEAVIER_THAN_MOE_DOUBLE, homerSimpson);
    	assertFilterContainsOnlyExpectedMembers("weightFloat", String.valueOf(moeSzyslak.getWeightFloat()), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	// no boolean is greater than true
    	assertFilterIsEmpty("gold", "true");
    	
    	// filters gold == true as (true > false) == true
    	assertFilterContainsOnlyExpectedMembers("gold", "false", joeQuimby);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterContainsOnlyExpectedMembers("goldObj", "true");
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterContainsOnlyExpectedMembers("goldObj", "false", joeQuimby);
    }
    
    @Test
    public void filtersByString() {
    	assertFilterContainsOnlyExpectedMembers("lastName", "Simpson", moeSzyslak);
    	
    	assertFilterContainsOnlyExpectedMembers("lastName", "S", homerSimpson, margeSimpson, moeSzyslak);
    	
    	// but with lower case...
    	assertFilterIsEmpty("lastName", "s");
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-01", moeSzyslak);
    	
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-02");
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "01-03-2015", "dd-MM-yyyy",  moeSzyslak);
    	assertFilterIsEmpty("registrationDate", "02-03-2015", "dd-MM-yyyy");
    }

	@Test
	public void rejectsMissingArgument() throws ParseException {
		assertThatThrownBy(() -> new GreaterThan<>(queryCtx, "path", new String[] {}, defaultConverter))
				.isInstanceOf(IllegalArgumentException.class);;
	}

	@Test
	public void rejectsTooManyArguments() throws ParseException {
		assertThatThrownBy(() -> new GreaterThan<>(queryCtx, "path", new String[] { "2014-03-10", "2014-03-11", "2014-03-11" }, defaultConverter))
				.isInstanceOf(IllegalArgumentException.class);;
	}

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(GreaterThan.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
    
}
