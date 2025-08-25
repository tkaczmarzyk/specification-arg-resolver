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

/**
 * Defines strategies for case-insensitive string comparisons in specifications.
 */
public enum IgnoreCaseStrategy {
    
    /**
     * Uses database UPPER() function for both sides of comparison.
     * This is the default strategy for compatibility with Spring Data JPA.
     * Example SQL: WHERE UPPER(column) = UPPER(value)
     * Strategy used by default.
     */
    DATABASE_UPPER,
    
    /**
     * Uses database LOWER() function for both sides of comparison.
     * May be preferable for certain locales (e.g., German with ß character).
     * Example SQL: WHERE LOWER(column) = LOWER(value)
     */
    DATABASE_LOWER,
    
    /**
     * Uses application-side case conversion.
     * Warning: This strategy may lead to inconsistent results when the application
     * and database use different case conversion rules (e.g., German ß character).
     * 
     * @deprecated Use {@link #DATABASE_UPPER} or {@link #DATABASE_LOWER} for consistent behavior.
     *             This option is kept for backward compatibility only.
     */
    @Deprecated(since = "3.2", forRemoval = true)
    APPLICATION
}
