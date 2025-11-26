/**
 * Copyright 2014-2025 the original author or authors.
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

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Helper for easier joining lists of specs with {@code AND} operator
 *
 * @author Tomasz Kaczmarzyk
 */
public class Conjunction<T> implements Specification<T>, FakeSpecWrapper<T> {

	private static final long serialVersionUID = 1L;

	private Collection<Specification<T>> innerSpecs;

	/**
	 * In case of paged search, method {@link #toPredicate(Root, CriteriaQuery, CriteriaBuilder)}
	 * will be executed on the same object in two different contexts.
	 *
	 * The first time during standard search.
	 * The second time during count(*) query which is executed for paging purposes.
	 *
	 * Fakes should be initialized in both.
	 */
	private Set<CriteriaQuery<?>> queriesWithInitializedFakes = new HashSet<>();

	@SafeVarargs
	public Conjunction(Specification<T>... innerSpecs) {
		this(Arrays.asList(innerSpecs));
	}

	public Conjunction(Collection<Specification<T>> innerSpecs) {
		this.innerSpecs = innerSpecs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeFakes(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		if (!queriesWithInitializedFakes.contains(query)) {
			for (Specification<T> spec : innerSpecs) {
				if (spec instanceof FakeSpecWrapper) {
					((FakeSpecWrapper<T>) spec).initializeFakes(root, query, cb);
				}
				if (spec instanceof Fake) {
					spec.toPredicate(root, query, cb);
					continue;
				}
			}
		}
		queriesWithInitializedFakes.add(query);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    	initializeFakes(root, query, cb);

		return cb.and(
				innerSpecs.stream()
						.filter(spec -> !(spec instanceof Fake))
						.map(spec -> spec.toPredicate(root, query, cb))
						.filter(Objects::nonNull)
						.collect(toList()).toArray(new Predicate[]{})
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(innerSpecs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Conjunction other = (Conjunction) obj;
		return Objects.equals(innerSpecs, other.innerSpecs);
	}

	@Override
	public String toString() {
		return "Conjunction [innerSpecs=" + innerSpecs + "]";
	}
}
