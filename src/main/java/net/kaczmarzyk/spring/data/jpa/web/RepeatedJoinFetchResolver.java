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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.RepeatedJoinFetch;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jakub Radlica
 */
public class RepeatedJoinFetchResolver implements SpecificationResolver<RepeatedJoinFetch> {

	private JoinFetchSpecificationResolver joinFetchSpecificationResolver = new JoinFetchSpecificationResolver();

	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return RepeatedJoinFetch.class;
	}

	@Override
	public Specification<Object> buildSpecification(WebRequestProcessingContext context, RepeatedJoinFetch def) {
		Collection<Specification<Object>> joins = new ArrayList<>();

		for (JoinFetch fetchDef : def.value()) {
			joins.add(joinFetchSpecificationResolver.buildSpecification(context, fetchDef));
		}

		return new Conjunction<>(joins);
	}
}
