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
public class GreaterThanTest extends ComparableTestBase {
	
	@Override
    protected Specification<Customer> make(String path, String[] value, String[] config) {
    	return new GreaterThan<Customer>(path, value, config);
    }
    
    
    @Test
    public void filtersByEnumValue() {
        assertFilterMembers("gender", "MALE", margeSimpson);
        assertFilterMembers("gender", "FEMALE");
        assertFilterEmpty("gender", "OTHER");
    }
    
    @Test
    public void filtersByEnumString() {
        assertFilterMembers("genderAsString", "MALE");
        assertFilterMembers("genderAsString", "FEMALE", homerSimpson, moeSzyslak);
        assertFilterEmpty("genderAsString", "OTHER");
    }
    
    @Test
    public void filtersByLongValue() {
    	assertFilterMembers("id", moeSzyslak.getId().toString(), joeQuimby);
    }
    
    @Test
    public void filtersByPrimitiveLongValue() {
    	assertFilterMembers("weightLong", HEAVIER_THAN_MARGE, moeSzyslak, homerSimpson);
    }
    
    @Test
    public void filtersByIntegerValue() {
    	assertFilterMembers("weight", moeSzyslak.getWeight().toString(), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveIntValue() {
    	assertFilterMembers("weightInt", String.valueOf(moeSzyslak.getWeightInt()), homerSimpson);
    }
    
    @Test
    public void filtersByDoubleValue() {
    	assertFilterMembers("weightDouble", HEAVIER_THAN_MOE_DOUBLE, homerSimpson);
    	assertFilterMembers("weightDouble", moeSzyslak.getWeightDouble().toString(), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveFloatValue() {
    	assertFilterMembers("weightFloat", HEAVIER_THAN_MOE_DOUBLE, homerSimpson);
    	assertFilterMembers("weightFloat", String.valueOf(moeSzyslak.getWeightFloat()), homerSimpson);
    }
    
    @Test
    public void filtersByPrimitiveBooleanValue() {
    	// no boolean is greater than true
    	assertFilterEmpty("gold", "true");
    	
    	// filters gold == true as (true > false) == true
    	assertFilterMembers("gold", "false", joeQuimby);
    }
    
    @Test
    public void filtersByBooleanValue() {
    	assertFilterMembers("goldObj", "true");
    	
    	// filters goldObj... or no goldObj, as (true >= false) == true, but not the nulls
    	assertFilterMembers("goldObj", "false", joeQuimby);
    }
    
    @Test
    public void filtersByString() {
    	assertFilterMembers("lastName", "Simpson", moeSzyslak);
    	
    	assertFilterMembers("lastName", "S", homerSimpson, margeSimpson, moeSzyslak);
    	
    	// but with lower case...
    	assertFilterEmpty("lastName", "s");
    }
    
    @Test
    public void filtersByDateWithDefaultDateFormat() {
    	assertFilterMembers("registrationDate", "2015-03-01", moeSzyslak);
    	
    	assertFilterMembers("registrationDate", "2015-03-02");
    }
    
    @Test
    public void filterByDateWithCustomDateFormat() {
    	assertFilterMembers("registrationDate", "01-03-2015", "dd-MM-yyyy",  moeSzyslak);
    	assertFilterEmpty("registrationDate", "02-03-2015", "dd-MM-yyyy");
    }
    
}
