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
 * Tests for the LessThanOrEqual Specification.
 * 
 * @author TP Diffenbach
 */
public class LessThanOrEqualTest extends ComparableTestBase {

	@Override
    protected Specification<Customer> makeUUT(String path, String[] value, Converter config) {
    	return new LessThanOrEqual<Customer>(queryCtx, path, value, config);
    }
    
	
    @Test
    public void filtersByEnumValue() {
        assertFilterContainsOnlyExpectedMembers("gender", "MALE", homerSimpson, moeSzyslak);

        assertFilterContainsOnlyExpectedMembers("gender", "FEMALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterContainsOnlyExpectedMembers("gender", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterContainsOnlyExpectedMembers("genderAsString", "MALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterContainsOnlyExpectedMembers("genderAsString", "FEMALE", margeSimpson);

        assertFilterContainsOnlyExpectedMembers("genderAsString", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterContainsOnlyExpectedMembers("id", moeSzyslak.getId().toString(), moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterContainsOnlyExpectedMembers("weightLong", String.valueOf(margeSimpson.getWeightLong()), margeSimpson, joeQuimby); // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterContainsOnlyExpectedMembers("weight", margeSimpson.getWeight().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterContainsOnlyExpectedMembers("weightInt", String.valueOf(margeSimpson.getWeightInt()), margeSimpson, joeQuimby);  // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterContainsOnlyExpectedMembers("weightDouble", HEAVIER_THAN_MOE_DOUBLE, moeSzyslak, margeSimpson);
    	assertFilterContainsOnlyExpectedMembers("weightDouble", margeSimpson.getWeightDouble().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterContainsOnlyExpectedMembers("weightFloat", String.valueOf(moeSzyslak.getWeightFloat()), margeSimpson, moeSzyslak, joeQuimby);
    	assertFilterContainsOnlyExpectedMembers("weightFloat", Float.toString(margeSimpson.getWeightFloat()), margeSimpson, joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	assertFilterContainsOnlyExpectedMembers("gold", "true", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	// filters gold... or no gold, as (true >= false) == true
    	assertFilterContainsOnlyExpectedMembers("gold", "false", homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterContainsOnlyExpectedMembers("goldObj", "true", joeQuimby, moeSzyslak);
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterContainsOnlyExpectedMembers("goldObj", "false", moeSzyslak);
    }
    
    @Test
    public void filtersByString() {
    	assertFilterContainsOnlyExpectedMembers("lastName", "Szyslak", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	assertFilterContainsOnlyExpectedMembers("lastName", "S", joeQuimby);
    	
    	// but with lower case...
    	assertFilterContainsOnlyExpectedMembers("lastName", "s", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-01", homerSimpson, margeSimpson);
    	
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-02", moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "01-03-2015", "dd-MM-yyyy", homerSimpson, margeSimpson);
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "03-03-2015", "dd-MM-yyyy", moeSzyslak, homerSimpson, margeSimpson);
    }
    

}
