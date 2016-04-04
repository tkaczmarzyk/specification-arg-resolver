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

import org.hamcrest.CoreMatchers;
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
public class LessThanOrEqualTest extends ComparableTestBase {

	@Override
    protected Specification<Customer> make(String path, String[] value, String[] config) {
    	return new LessThanOrEqual<Customer>(path, value, config);
    }
    
	
    @Test
    public void filtersByEnumValue() {
        assertFilterMembers("gender", "MALE", homerSimpson, moeSzyslak);

        assertFilterMembers("gender", "FEMALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterMembers("gender", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterMembers("genderAsString", "MALE", homerSimpson, moeSzyslak, margeSimpson);

        assertFilterMembers("genderAsString", "FEMALE", margeSimpson);

        assertFilterMembers("genderAsString", "OTHER", homerSimpson, moeSzyslak, margeSimpson);
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterMembers("id", moeSzyslak.getId().toString(), moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterMembers("weightLong", String.valueOf(margeSimpson.getWeightLong()), margeSimpson, joeQuimby); // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterMembers("weight", margeSimpson.getWeight().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterMembers("weightInt", String.valueOf(margeSimpson.getWeightInt()), margeSimpson, joeQuimby);  // Joe's null maps to zero for primitive
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterMembers("weightDouble", HEAVIER_THAN_MOE_DOUBLE, moeSzyslak, margeSimpson);
    	assertFilterMembers("weightDouble", margeSimpson.getWeightDouble().toString(), margeSimpson);
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterMembers("weightFloat", String.valueOf(moeSzyslak.getWeightFloat()), margeSimpson, moeSzyslak, joeQuimby);
    	assertFilterMembers("weightFloat", Float.toString(margeSimpson.getWeightFloat()), margeSimpson, joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	assertFilterMembers("gold", "true", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	// filters gold... or no gold, as (true >= false) == true
    	assertFilterMembers("gold", "false", homerSimpson, margeSimpson, moeSzyslak);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterMembers("goldObj", "true", joeQuimby, moeSzyslak);
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterMembers("goldObj", "false", moeSzyslak);
    }
    
    @Test
    public void filtersByString() {
    	assertFilterMembers("lastName", "Szyslak", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    	
    	assertFilterMembers("lastName", "S", joeQuimby);
    	
    	// but with lower case...
    	assertFilterMembers("lastName", "s", homerSimpson, margeSimpson, moeSzyslak, joeQuimby);
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterMembers("registrationDate", "2015-03-01", homerSimpson, margeSimpson);
    	
    	assertFilterMembers("registrationDate", "2015-03-02", moeSzyslak, homerSimpson, margeSimpson);
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterMembers("registrationDate", "01-03-2015", "dd-MM-yyyy", homerSimpson, margeSimpson);
    	assertFilterMembers("registrationDate", "03-03-2015", "dd-MM-yyyy", moeSzyslak, homerSimpson, margeSimpson);
    }
    

}
