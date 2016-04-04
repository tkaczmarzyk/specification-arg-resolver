/**
 * Copyright 2014-2016 the original author or authors.
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

import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.ComparableTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;


/**
 * @author Tomasz Kaczmarzyk
 * @author Maciej Szewczyszyn
 * @author TP Diffenbach
 */
public class LessThanTest extends ComparableTestBase {

	@Override
    protected Specification<Customer> make(String path, String[] value, String[] config) {
    	return new LessThan<Customer>(path, value, config);
    }
    
        
    @Test
    public void filtersByEnumValue() {
        assertFilterMembers("gender", "MALE");
        assertFilterMembers("gender", "FEMALE", homerSimpson, moeSzyslak);
        assertFilterMembers("gender", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterMembers("genderAsString", "MALE", margeSimpson);
        assertFilterMembers("genderAsString", "FEMALE");
        assertFilterMembers("genderAsString", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterMembers("id", homerSimpson.getId().toString());
    	assertFilterMembers("id", moeSzyslak.getId().toString(), homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterMembers("weightLong", String.valueOf(margeSimpson.getWeightLong()), joeQuimby); // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterMembers("weight", margeSimpson.getWeight().toString());
    }
    
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterMembers("weightInt", String.valueOf(margeSimpson.getWeightInt()), joeQuimby);  // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterMembers("weightDouble", Double.toString(margeSimpson.getWeightDouble() + 0.0001), margeSimpson);
    	assertFilterMembers("weightDouble", Double.toString(margeSimpson.getWeightDouble()));
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterMembers("weightFloat", HEAVIER_THAN_MOE_DOUBLE, margeSimpson, moeSzyslak, joeQuimby);
    	assertFilterMembers("weightFloat", Double.toString(margeSimpson.getWeightFloat() - 0000.1), joeQuimby);
    	
    	//this test fails:
    	//assertFilterMembers("weightFloat", Double.toString(margeSimpson.getWeightFloat()), joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	assertFilterMembers("gold", "true", homerSimpson, margeSimpson, moeSzyslak);
    	
    	// filters gold... or no gold, as (true >= false) == true
    	assertFilterMembers("gold", "false");
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterMembers("goldObj", "true", moeSzyslak);
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterMembers("goldObj", "false");
    }
    
    @Test
    public void filtersByString() {
    	assertFilterMembers("lastName", "Szyslak", homerSimpson, margeSimpson, joeQuimby);
    	
    	assertFilterMembers("lastName", "S", joeQuimby);
    	
    	// but with lower case...
    	assertFilterMembers("lastName", "s", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterMembers("registrationDate", "2015-03-01");
    	
    	assertFilterMembers("registrationDate", "2015-03-02", homerSimpson, margeSimpson);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterMembers("registrationDate", "01-03-2015", "dd-MM-yyyy");
    	assertFilterMembers("registrationDate", "03-03-2015", "dd-MM-yyyy", homerSimpson, margeSimpson, moeSzyslak);
    }
  
}
