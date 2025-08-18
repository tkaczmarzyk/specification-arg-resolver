/*
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

import java.util.Locale;

/**
 * <p>Specifications that implement this interface will be provided with Locale
 * after instantiation. Locale is important e.g. when {@code String.toUpperCase} is used
 * (typically in case-insensitive comparisons).</p> 
 * 
 * @author Tomasz Kaczmarzyk
 * 
 * @see EqualIgnoreCase
 * @see LikeIgnoreCase
 */
public interface LocaleAware {

	void setLocale(Locale locale);
}
