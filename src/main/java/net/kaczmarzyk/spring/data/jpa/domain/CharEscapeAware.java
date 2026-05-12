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

import net.kaczmarzyk.spring.data.jpa.utils.CharEscaper;

/**
 * Interface to be implemented by specifications that support character escaping.
 *
 * @since 3.4
 * @author Sebastian Nawrocki
 */
public interface CharEscapeAware {

    /**
     * Applies the provided {@link CharEscaper} to the specification.
     *
     * @param charEscaper the escaper to be used
     */
    void applyCharEscaper(CharEscaper charEscaper);

}
