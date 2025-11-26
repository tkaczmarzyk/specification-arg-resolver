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
package net.kaczmarzyk.spring.data.jpa.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import net.kaczmarzyk.spring.data.jpa.domain.IgnoreCaseStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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

        if (effectiveStrategy == IgnoreCaseStrategy.APPLICATION) {
            Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
            String convertedValue = value.toUpperCase(effectiveLocale);
            return new ConvertedExpressions(
                    cb.upper(columnExpression),
                    cb.literal(convertedValue)
            );
        }

        return new ConvertedExpressions(
                applyDatabaseCaseConversion(cb, columnExpression, effectiveStrategy),
                applyDatabaseCaseConversion(cb, cb.literal(value), effectiveStrategy)
        );
    }

    /**
     * Applies case conversion to both column expression and value according to the strategy.
     */
    @SuppressWarnings("deprecation")
    public static ConvertedExpressionsList applyCaseConversion(
            CriteriaBuilder cb,
            Expression<String> columnExpression,
            String[] values,
            IgnoreCaseStrategy strategy,
            Locale locale) {

        IgnoreCaseStrategy effectiveStrategy = strategy != null ? strategy : IgnoreCaseStrategy.DATABASE_UPPER;

        if (effectiveStrategy == IgnoreCaseStrategy.APPLICATION) {
            Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
            List<Expression<String>> convertedValues = Arrays.stream(values)
                    .map(value -> cb.literal(value.toUpperCase(effectiveLocale)))
                    .toList();
            return new ConvertedExpressionsList(
                    cb.upper(columnExpression),
                    convertedValues
            );
        }

        return new ConvertedExpressionsList(
                applyDatabaseCaseConversion(cb, columnExpression, effectiveStrategy),
                Arrays.stream(values)
                        .map(value -> applyDatabaseCaseConversion(cb, cb.literal(value), effectiveStrategy))
                        .toList()
        );
    }

    private static Expression<String> applyDatabaseCaseConversion(
            CriteriaBuilder cb,
            Expression<String> expression,
            IgnoreCaseStrategy strategy) {

        IgnoreCaseStrategy effectiveStrategy = strategy != null ? strategy : IgnoreCaseStrategy.DATABASE_UPPER;

        return switch (effectiveStrategy) {
            case DATABASE_UPPER -> cb.upper(expression);
            case DATABASE_LOWER -> cb.lower(expression);
            default -> throw new IllegalArgumentException("Unknown case strategy: " + effectiveStrategy);
        };
    }

    public record ConvertedExpressions(
            Expression<String> column,
            Expression<String> value
    ) {

        /**
         * Creates a new instance with the given expressions.
         *
         * @param column the column expression (must not be null)
         * @param value  the value expression (must not be null)
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

    public record ConvertedExpressionsList(
            Expression<String> column,
            List<Expression<String>> values
    ) {

        /**
         * Creates a new instance with the given expressions.
         *
         * @param column the column expression (must not be null)
         * @param values the list of value expression (must not be empty and not contain null)
         * @throws IllegalArgumentException if either expression violates its nullability constraints
         */
        public ConvertedExpressionsList {
            if (column == null) {
                throw new IllegalArgumentException("Column expression must not be null");
            }
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("Values list must not be null or empty");
            }
            if (values.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Values list must not contain null elements");
            }
        }
    }
}
