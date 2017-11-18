package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <p>Filters with not equal where-clause (e.g. {@code where firstName <> "Homer"}).</p>
 *
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 *
 * <p>If the field type is string, the where-clause is case insensitive</p>
 *
 * @author Mateusz Fedkowicz
 **/
public class NotEqualIgnoreCase<T> extends NotEqual<T> {

	public NotEqualIgnoreCase(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path, httpParamValues, converter);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return path(root).getJavaType().equals(String.class)
				? cb.notEqual(cb.upper(this.<String>path(root)), expectedValue.toUpperCase())
				: super.toPredicate(root, query, cb);
	}

}
