/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @deprecated
 * This is going to be removed with {@link net.kaczmarzyk.spring.data.jpa.web.annotation.Joins}
 *
 * @author Tomasz Kaczmarzyk
 */
@Deprecated
class JoinsSpecificationResolver implements SpecificationResolver<Joins> {

	private JoinFetchSpecificationResolver joinFetchSpecificationResolver = new JoinFetchSpecificationResolver();
	private JoinSpecificationResolver joinSpecificationResolver = new JoinSpecificationResolver();

	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return Joins.class;
	}

	@Override
	public Specification<Object> buildSpecification(ProcessingContext context, Joins joinsDef) {
		Collection<Specification<Object>> joins = new ArrayList<>();

		for (JoinFetch fetchDef : joinsDef.fetch()) {
			joins.add(joinFetchSpecificationResolver.buildSpecification(context, fetchDef));
		}

		for (Join joinDef : joinsDef.value()) {
			joins.add(joinSpecificationResolver.buildSpecification(context, joinDef));
		}

		return new Conjunction<>(joins);
	}
}
