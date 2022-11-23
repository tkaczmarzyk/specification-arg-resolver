/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.Test;

import java.sql.Timestamp;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Radlica
 */
public class StringToTimestampConverterTest {

    Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);

    @Test
    public void convertsToTimestampUsingDefaultFormat() {
        Timestamp timestamp = converterWithDefaultFormats.convert("2022-11-21T15:08:53.282Z", Timestamp.class);

        assertThat(timestamp)
                .hasYear(2022)
                .hasMonth(11)
                .hasDayOfMonth(21)
                .hasHourOfDay(15)
                .hasMinute(8)
                .hasSecond(53)
                .hasMillisecond(282);
    }

    @Test
    public void throwsValueRejectedExceptionForUnparseableTimestamp() {
        assertThrows(
                ValueRejectedException.class,
                () -> converterWithDefaultFormats.convert("2020-15:08:53.282+02:0006-16T", Timestamp.class),
                "Timestamp format exception, expected format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        );
    }

    @Test
    public void convertsToTimestampUsingCustomFormat() {
        Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSMM-dd'T'", EMPTY_RESULT, null);
        Timestamp timestamp = converterWithCustomFormat.convert("2020-15:08:53.28206-16T", Timestamp.class);

        assertThat(timestamp)
                .hasYear(2020)
                .hasMonth(6)
                .hasDayOfMonth(16)
                .hasHourOfDay(15)
                .hasMinute(8)
                .hasSecond(53)
                .hasMillisecond(282);
    }

    @Test
    public void throwsValueRejectedExceptionForUnparseableTimestampAndCustomFormat() {
        Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-invalid-format-HH:mm:ss", EMPTY_RESULT, null);

        assertThrows(
                ValueRejectedException.class,
                () -> converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", Timestamp.class),
                "Timestamp format exception, expected format: yyyy-invalid-format-HH:mm:ss"
        );

    }
}
