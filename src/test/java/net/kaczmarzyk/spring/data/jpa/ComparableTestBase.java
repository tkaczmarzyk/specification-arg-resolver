package net.kaczmarzyk.spring.data.jpa;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;

public abstract class ComparableTestBase extends IntegrationTestBase {

	protected static final String HEAVIER_THAN_MOE_DOUBLE = "65.21";
	protected static final String HEAVIER_THAN_MARGE = "56";
	
	protected Customer homerSimpson;
	protected Customer margeSimpson;
	protected Customer moeSzyslak;
	protected Customer joeQuimby;

	@Before
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
		margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
		moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 03, 02).weight(65).notGolden().build(em);
		joeQuimby = customer("Joe", "Quimby").golden().build(em); // Gender nor Weight nor Registration Date not specifed
	}

	protected abstract Specification<Customer> make(String path, String[] value, String[] config);

	protected Specification<Customer> make(String path, String value, String[] config) {
		return make(path, new String[] { value }, config);
	}
	
	protected Specification<Customer> make(String path, String value, String config) {
		return make(path, new String[] { value }, config == null ? null : new String[] { config });
	}

	protected Specification<Customer> make(String path, String value) {
		return make(path, value, (String[]) null);
	}

	protected void assertFilterMembers(String path, String value, Customer... members) {
		assertFilterMembers(make(path, value), members);
	}

	// Redundant, as assertFilterMembers(path, value) works just as well.
	// But retained as the name suggests the expected outcome.
	protected void assertFilterEmpty(String path, String value) {
		assertFilterEmpty(make(path, value));
	}

	protected void assertFilterMembers(String path, String value, String config, Customer... members) {
		assertFilterMembers(make(path, value, config), members);
	}

	// Redundant, as assertFilterMembers(path, value) works just as well.
	// But retained as the name suggests the expected outcome.
	protected void assertFilterEmpty(String path, String value, String config) {
		assertFilterEmpty(make(path, value, config));
	}
	
    @Test
    public void rejectsNotExistingEnumConstantName() {
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("could not find value ROBOT for enum class Gender");
        customerRepo.findAll(make("gender", "ROBOT"));
    }
	
    @Test
    public void rejectsInvalidConfig_zeroArguments() {
    	String[] emptyConfig = new String[] {};

    	expectedException.expect(IllegalArgumentException.class);
    	
    	make("registrationDate", "01-03-2015", emptyConfig);
    }
    
    @Test
    public void rejectsInvalidConfig_tooManyArgument() {
    	String[] invalidConfig = new String[] {"yyyy-MM-dd", "unexpected"};
    	
    	expectedException.expect(IllegalArgumentException.class);
    	
    	make("registrationDate", "01-03-2015", invalidConfig);
    }

    @Test
    public void rejectsNonIntegerArguments() {
    	expectedException.expect(InvalidDataAccessApiUsageException.class);
    	assertFilterMembers("weight", "1.1");
    }
    
    @Test
    public void rejectsNonNumericArguments() {
    	expectedException.expect(InvalidDataAccessApiUsageException.class);
    	assertFilterMembers("weightDouble", "one");
    }
}