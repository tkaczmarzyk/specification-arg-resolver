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

import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;

/**
 * @author Tomasz Kaczmarzyk
 */
class JoinSpecificationResolver implements SpecificationResolver<Join> {

	@Override
	public Class<? extends Annotation> getSupportedSpecificationDefinition() {
		return Join.class;
	}

	@Override
	public Specification<Object> buildSpecification(WebRequestProcessingContext context, Join joinDef) {
		return new net.kaczmarzyk.spring.data.jpa.domain.Join<Object>(context.queryContext(), joinDef.path(), joinDef.alias(), joinDef.type(), joinDef.distinct());
	}

}
