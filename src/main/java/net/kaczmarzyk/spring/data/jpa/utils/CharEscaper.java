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

import java.util.*;

/**
 * Component responsible for escaping special characters.
 *
 * @since 3.4
 * @author Sebastian Nawrocki
 */
public class CharEscaper {

    public static final CharEscaper DISABLED = new CharEscaper(null, Set.of());

    private final Character escapeChar;
    private final Set<Character> charsToEscape;

    public CharEscaper(Character escapeChar, Collection<Character> charsToEscape) {
        this.escapeChar = escapeChar;
        this.charsToEscape = Set.copyOf(charsToEscape);
    }

    /**
     * Performs escaping of the provided value using the configured escape character
     * and characters to be escaped.
     *
     * @param value raw value to be escaped
     * @return escaped value, or null if the input was null
     */
    public String escape(String value) {
        if (escapeChar == null || value == null) {
            return value;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == escapeChar || charsToEscape.contains(c)) {
                sb.append(escapeChar);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public Character getEscapeChar() {
        return escapeChar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CharEscaper that = (CharEscaper) o;
        return Objects.equals(escapeChar, that.escapeChar) && Objects.equals(charsToEscape, that.charsToEscape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(escapeChar, charsToEscape);
    }

    @Override
    public String toString() {
        return "CharEscaper[" +
                "escapeChar='" + escapeChar + '\'' +
                ", charsToEscape=" + charsToEscape +
                ']';
    }
}
