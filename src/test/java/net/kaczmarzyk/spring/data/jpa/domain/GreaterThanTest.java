/**
 * Copyright 2014-2020 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.ComparableTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;


/**
 * Tests for the GreaterThan Specification.
 * 
 * @author TP Diffenbach
 */
public class GreaterThanTest extends ComparableTestBase {
	
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
    
}
