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
 * @author Mateusz Fedkowicz
 **/
public class NotEqual<T> extends PathSpecification<T> {

	protected String expectedValue;
	private Converter converter;

	public NotEqual(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException();
		}
		this.expectedValue = httpParamValues[0];
		this.converter = converter;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Class<?> typeOnPath = path(root).getJavaType();
		return cb.notEqual(path(root), converter.convert(expectedValue, typeOnPath));
	}

}
