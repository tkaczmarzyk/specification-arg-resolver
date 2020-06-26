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
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomasz Kaczmarzyk
 */
class AndSpecificationResolver implements SpecificationResolver<And> {

	private SimpleSpecificationResolver specResolver;
	
	public AndSpecificationResolver(SimpleSpecificationResolver simpleSpecificationResolver) {
		this.specResolver = simpleSpecificationResolver;
	}
	
	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return And.class;
	}

	@Override
	public Specification<Object> buildSpecification(WebRequestProcessingContext context, And def) {
		List<Specification<Object>> innerSpecs = new ArrayList<Specification<Object>>();
		for (Spec innerDef : def.value()) {
			Specification<Object> innerSpec = specResolver.buildSpecification(context, innerDef);
			if (innerSpec != null) {
				innerSpecs.add(innerSpec);
			}
		}

		return innerSpecs.isEmpty() ? null : new Conjunction<>(innerSpecs);
	}

}
