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
package net.kaczmarzyk.spring.data.jpa.web.annotation;

import java.util.List;
import static net.kaczmarzyk.spring.data.jpa.utils.Converter.ValuesRejectedException;

import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.domain.EmptyResultOnTypeMismatch;
import net.kaczmarzyk.spring.data.jpa.domain.WithoutTypeConversion;

/**
 * <p>Specifies the behaviour in case of type mismatch between HTTP param value
 * and the type on the property being filtered (e.g. when HTTP param = {@code "abc"} and the type is {@code Long}).</p>
 * 
 * <p>To be used with {@code onTypeMismatch} property of {@code @Spec} annotation.</p>
 * 
 * @author Tomasz Kaczmarzyk
 */
public enum OnTypeMismatch {

	EXCEPTION {
		@Override
		public <T> Specification<T> wrap(Specification<T> spec) {
			return spec;
		}

		@Override
		void doHandleRejectedValues(List<String> rejected) {
			throw new ValuesRejectedException(rejected, "invalid values present in the HTTP param");
		}
	},
	EMPTY_RESULT {
		@Override
		public <T> Specification<T> wrap(Specification<T> spec) {
			if (spec instanceof WithoutTypeConversion) {
				return spec;
			}
			return new EmptyResultOnTypeMismatch<>(spec);
		}

		@Override
		void doHandleRejectedValues(List<String> rejected) {
			// do nothing
		}
	}, DEFAULT {
		@Override
		public <T> Specification<T> wrap(Specification<T> spec) {
			return OnTypeMismatch.EMPTY_RESULT.wrap(spec);
		}

		@Override
		void doHandleRejectedValues(List<String> rejected) {
			OnTypeMismatch.EMPTY_RESULT.doHandleRejectedValues(rejected);
		}
	};

	public abstract <T> Specification<T> wrap(Specification<T> spec);

	public void handleRejectedValues(List<String> rejected) {
		if (rejected != null && !rejected.isEmpty()) {
			doHandleRejectedValues(rejected);
		}
	}

	abstract void doHandleRejectedValues(List<String> rejected);
}
