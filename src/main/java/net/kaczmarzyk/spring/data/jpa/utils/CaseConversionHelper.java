/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import net.kaczmarzyk.spring.data.jpa.domain.IgnoreCaseStrategy;

import java.util.Locale;

/**
 * Helper class for applying case conversion strategies in specifications.
 * 
 * @author Jakub Radlica
 * @since 3.2
 */
public class CaseConversionHelper {
    
    /**
     * Applies case conversion to both column expression and value according to the strategy.
     */
    @SuppressWarnings("deprecation")
    public static ConvertedExpressions applyCaseConversion(
            CriteriaBuilder cb,
            Expression<String> columnExpression,
            String value,
            IgnoreCaseStrategy strategy,
            Locale locale) {
        
        IgnoreCaseStrategy effectiveStrategy = strategy != null ? strategy : IgnoreCaseStrategy.DATABASE_UPPER;
        
        switch (effectiveStrategy) {
            case DATABASE_UPPER:
                return new ConvertedExpressions(
                    cb.upper(columnExpression),
                    cb.upper(cb.literal(value))
                );
                
            case DATABASE_LOWER:
                return new ConvertedExpressions(
                    cb.lower(columnExpression),
                    cb.lower(cb.literal(value))
                );
                
            case APPLICATION:
                Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
                String convertedValue = value.toUpperCase(effectiveLocale);
                return new ConvertedExpressions(
                    cb.upper(columnExpression),
                    cb.literal(convertedValue)
                );
                
            default:
                throw new IllegalArgumentException("Unknown case strategy: " + effectiveStrategy);
        }
    }
    
    public record ConvertedExpressions(
            Expression<String> column,
            Expression<String> value
    ) {
        
        /**
         * Creates a new instance with the given expressions.
         *
         * @param column the column expression (must not be null)
         * @param value the value expression (must not be null)
         * @throws IllegalArgumentException if either expression is null
         */
        public ConvertedExpressions {
            if (column == null) {
                throw new IllegalArgumentException("Column expression must not be null");
            }
            if (value == null) {
                throw new IllegalArgumentException("Value expression must not be null");
            }
        }
    }
}
