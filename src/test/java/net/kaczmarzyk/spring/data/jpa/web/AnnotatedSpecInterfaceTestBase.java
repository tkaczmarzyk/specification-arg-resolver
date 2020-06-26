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

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;



public abstract class AnnotatedSpecInterfaceTestBase extends ResolverTestBase {

	protected Converter converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);
	protected SpecificationArgumentResolver specificationArgumentResolver = new SpecificationArgumentResolver();

	public Equal<Object> equal(WebRequestProcessingContext ctx, String path, String value) {
		return new Equal<>(ctx.queryContext(), path, new String[]{value}, converter);
	}

	public In<Object> in(WebRequestProcessingContext ctx, String path, String... values) {
		return new In<>(ctx.queryContext(), path, values, converter);
	}

}
