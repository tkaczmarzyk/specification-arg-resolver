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

import java.util.Arrays;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * <p>Base class for Comparable comparisons..</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 * @author TP Diffenbach
 */
public abstract class ComparableSpecification<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;
	
	private String comparedTo;
	private Converter converter;	
	
	public ComparableSpecification(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException("expected one http-param, but was " + Arrays.toString(httpParamValues));
		}
		this.comparedTo = httpParamValues[0];
		this.converter = converter;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Expression<?> rootPath = path(root);
		Class<?> typeOnPath = rootPath.getJavaType();

		return makePredicate(cb, (Expression<? extends Comparable>) rootPath, 
				(Comparable) converter.convert(comparedTo, typeOnPath));
		
		//  the line below actually works (!), if Y doesn't need to extend Comparable. --tpd
		//return this.makePredicate(cb, rootPath.as(typeOnPath.asSubclass(typeOnPath)), 
		//		converter.convert(comparedTo, typeOnPath));
		
		//  the line below DOES work, but using the casts above is probably more efficient.
		//return this.makePredicate(cb, rootPath.as(typeOnPath.asSubclass(Comparable.class)), 
		//		(Comparable) converter.convert(comparedTo, typeOnPath));
	}
	
	protected abstract <Y extends Comparable<? super Y>> 
		Predicate makePredicate(CriteriaBuilder cb, Expression<? extends Y> x, Y y);
}
