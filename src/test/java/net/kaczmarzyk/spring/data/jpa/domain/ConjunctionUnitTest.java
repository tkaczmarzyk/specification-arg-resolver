package net.kaczmarzyk.spring.data.jpa.domain;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.data.jpa.domain.Specification;

import com.jayway.jsonpath.Criteria;

import net.kaczmarzyk.spring.data.jpa.Customer;

/**
 *
 * @author Tomasz Kaczmarzyk
 */
public class ConjunctionUnitTest {

	private static interface FakeSpec extends Specification<Customer>, Fake {
	}

	Root<Customer> root = mock(Root.class);
	CriteriaQuery<Customer> query = mock(CriteriaQuery.class);
	CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
	
	@Test
	public void executesFakeSpecBeforeRegularOne() {
		Specification<Customer> fakeSpec = mock(FakeSpec.class);
		Specification<Customer> regularSpec = mock(Like.class);
		
		Conjunction<Customer> conjunction = new Conjunction<>(fakeSpec, regularSpec);
		
		conjunction.toPredicate(root, query, criteriaBuilder);
		
		InOrder inOrder = inOrder(fakeSpec, regularSpec);
		inOrder.verify(fakeSpec).toPredicate(root, query, criteriaBuilder);
		inOrder.verify(regularSpec).toPredicate(root, query, criteriaBuilder);
	}
	
	@Test
	public void executesFakeSpecBeforeRegularOnes() {
		Specification<Customer> fakeSpec = mock(FakeSpec.class);
		Specification<Customer> regularSpec1 = mock(Like.class);
		Specification<Customer> regularSpec2 = mock(Equal.class);
		
		Conjunction<Customer> conjunction = new Conjunction<>(regularSpec2, fakeSpec, regularSpec1);
		
		conjunction.toPredicate(root, query, criteriaBuilder);
		
		InOrder inOrder = inOrder(fakeSpec, regularSpec1, regularSpec2);
		inOrder.verify(fakeSpec).toPredicate(root, query, criteriaBuilder);
		inOrder.verify(regularSpec1).toPredicate(root, query, criteriaBuilder);
		inOrder.verify(regularSpec2).toPredicate(root, query, criteriaBuilder);
	}
}
