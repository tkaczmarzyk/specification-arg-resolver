/*
 * Copyright 2014-2026 the original author or authors.
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

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Nawrocki
 */
public class CharEscaperTest {

    @Test
    public void escapesSpecifiedCharactersAtDifferentPositions() {
        CharEscaper escaper = new CharEscaper('\\', Set.of('%', '_'));

        assertThat(escaper.escape("%_percent_underscore")).isEqualTo("\\%\\_percent\\_underscore");
        assertThat(escaper.escape("percent_underscore%_")).isEqualTo("percent\\_underscore\\%\\_");
        assertThat(escaper.escape("per%cent_und_erscore")).isEqualTo("per\\%cent\\_und\\_erscore");
        assertThat(escaper.escape("combo%_combo")).isEqualTo("combo\\%\\_combo");
    }

    @Test
    public void escapesEscapeCharacterItself() {
        CharEscaper escaper = new CharEscaper('\\', Set.of('%', '_'));

        assertThat(escaper.escape("escape \\ character")).isEqualTo("escape \\\\ character");
    }

    @Test
    public void doesNotEscapeAnythingWhenDisabled() {
        CharEscaper escaper = CharEscaper.DISABLED;

        assertThat(escaper.escape("percent % and underscore _")).isEqualTo("percent % and underscore _");
    }

    @Test
    public void returnsNullWhenValueIsNull() {
        CharEscaper escaper = new CharEscaper('\\', Set.of('%', '_'));

        assertThat(escaper.escape(null)).isNull();
    }
}
