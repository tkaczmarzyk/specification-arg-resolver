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
 * Tests for the LessThan Specification.
 * 
 * @author TP Diffenbach
 */
public class LessThanTest extends ComparableTestBase {

	@Override
    protected Specification<Customer> makeUUT(String path, String[] value, Converter config) {
    	return new LessThan<Customer>(queryCtx, path, value, config);
    }
    
        
    @Test
    public void filtersByEnumValue() {
        assertFilterContainsOnlyExpectedMembers("gender", "MALE");
        assertFilterContainsOnlyExpectedMembers("gender", "FEMALE", homerSimpson, moeSzyslak);
        assertFilterContainsOnlyExpectedMembers("gender", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterContainsOnlyExpectedMembers("genderAsString", "MALE", margeSimpson);
        assertFilterContainsOnlyExpectedMembers("genderAsString", "FEMALE");
        assertFilterContainsOnlyExpectedMembers("genderAsString", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterContainsOnlyExpectedMembers("id", homerSimpson.getId().toString());
    	assertFilterContainsOnlyExpectedMembers("id", moeSzyslak.getId().toString(), homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterContainsOnlyExpectedMembers("weightLong", String.valueOf(margeSimpson.getWeightLong()), joeQuimby); // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterContainsOnlyExpectedMembers("weight", margeSimpson.getWeight().toString());
    }
    
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterContainsOnlyExpectedMembers("weightInt", String.valueOf(margeSimpson.getWeightInt()), joeQuimby);  // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterContainsOnlyExpectedMembers("weightDouble", Double.toString(margeSimpson.getWeightDouble() + 0.0001), margeSimpson);
    	assertFilterContainsOnlyExpectedMembers("weightDouble", Double.toString(margeSimpson.getWeightDouble()));
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterContainsOnlyExpectedMembers("weightFloat", HEAVIER_THAN_MOE_DOUBLE, margeSimpson, moeSzyslak, joeQuimby);
    	assertFilterContainsOnlyExpectedMembers("weightFloat", Double.toString(margeSimpson.getWeightFloat() - 0000.1), joeQuimby);
    	
    	//this test fails:
    	//assertFilterMembers("weightFloat", Double.toString(margeSimpson.getWeightFloat()), joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	assertFilterContainsOnlyExpectedMembers("gold", "true", homerSimpson, margeSimpson, moeSzyslak);
    	
    	// filters gold... or no gold, as (true >= false) == true
    	assertFilterContainsOnlyExpectedMembers("gold", "false");
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterContainsOnlyExpectedMembers("goldObj", "true", moeSzyslak);
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterContainsOnlyExpectedMembers("goldObj", "false");
    }
    
    @Test
    public void filtersByString() {
    	assertFilterContainsOnlyExpectedMembers("lastName", "Szyslak", homerSimpson, margeSimpson, joeQuimby);
    	
    	assertFilterContainsOnlyExpectedMembers("lastName", "S", joeQuimby);
    	
    	// but with lower case...
    	assertFilterContainsOnlyExpectedMembers("lastName", "s", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-01");
    	
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "2015-03-02", homerSimpson, margeSimpson);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "01-03-2015", "dd-MM-yyyy");
    	assertFilterContainsOnlyExpectedMembers("registrationDate", "03-03-2015", "dd-MM-yyyy", homerSimpson, margeSimpson, moeSzyslak);
    }
  
}
