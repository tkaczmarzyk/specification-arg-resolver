/**
 * Copyright 2014-2023 the original author or authors.
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
public class IsNotEmpty<T> extends PathSpecification<T> implements WithoutTypeConversion, ZeroArgSpecification {

	private static final long serialVersionUID = 1L;

	public IsNotEmpty(QueryContext queryContext, String path, String[] args) {
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
