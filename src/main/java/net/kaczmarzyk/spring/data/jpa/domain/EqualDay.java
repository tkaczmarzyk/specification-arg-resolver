package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.*;

import java.util.Objects;

import static net.kaczmarzyk.spring.data.jpa.utils.DateTimeUtils.endOfDay;
import static net.kaczmarzyk.spring.data.jpa.utils.DateTimeUtils.startOfDay;


public class EqualDay<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;

	private final String expectedDay;
	private final Converter converter;

	public EqualDay(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException();
		}
		this.converter = converter;
		this.expectedDay = httpParamValues[0];
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		Expression<Comparable<Object>> targetExpression = path(root);
		Class<?> typeOnPath = targetExpression.getJavaType();

		Object targetDayDate = converter.convert(expectedDay, typeOnPath);
		Object lowerBoundary = startOfDay(targetDayDate);
		Object upperBoundary = endOfDay(targetDayDate);
		return criteriaBuilder.between(targetExpression, (Comparable<Object>) lowerBoundary, (Comparable<Object>) upperBoundary);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		EqualDay<?> equalDay = (EqualDay<?>) o;
		return Objects.equals(expectedDay, equalDay.expectedDay) &&
				Objects.equals(converter, equalDay.converter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), expectedDay, converter);
	}
}
