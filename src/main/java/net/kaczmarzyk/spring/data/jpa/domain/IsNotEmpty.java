package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <p>Filters with is not empty where-clause - if collection from association is not empty (e.g. {@code where customer.orders is not empty}).</p>
 *
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class IsNotEmpty<T> extends PathSpecification<T> implements WithoutTypeConversion {

	private static final long serialVersionUID = 1L;

	public IsNotEmpty(QueryContext queryContext, String path) {
		super(queryContext, path);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		return builder.isNotEmpty(this.path(root));
	}

	@Override
	public String toString() {
		return "IsNotEmpty[" +
			"path='" + path + '\'' +
			']';
	}
}
