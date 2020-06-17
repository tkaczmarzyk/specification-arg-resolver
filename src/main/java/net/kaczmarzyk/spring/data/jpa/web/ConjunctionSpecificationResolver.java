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

import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Tomasz Kaczmarzyk
 */
class ConjunctionSpecificationResolver implements SpecificationResolver<Conjunction> {

	private SimpleSpecificationResolver specResolver;
	private OrSpecificationResolver orResolver;
	
	public ConjunctionSpecificationResolver(SimpleSpecificationResolver simpleSpecificationResolver) {
		this.specResolver = simpleSpecificationResolver;
		this.orResolver = new OrSpecificationResolver(simpleSpecificationResolver);
	}
	
	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return Conjunction.class;
	}

	public Specification<Object> buildSpecification(WebRequestProcessingContext context, Conjunction def) {
		List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
		for (Or innerOrDef : def.value()) {
			Specification<Object> innerOr = orResolver.buildSpecification(context, innerOrDef);
			if (innerOr != null) {
				innerSpecs.add(innerOr);
			}
		}
		for (Spec innerDef : def.and()) {
			Specification<Object> innerSpec = specResolver.buildSpecification(context, innerDef);
			if (innerSpec != null) {
				innerSpecs.add(innerSpec);
			}
		}

		return innerSpecs.isEmpty() ? null : new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(innerSpecs);
	}

}