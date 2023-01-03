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
package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
    public void throwsValueRejectedExceptionForUnparseableTimestamp_differentThanExpectedDateFormat() {
        assertThrows(
                ValueRejectedException.class,
                () -> converterWithDefaultFormats.convert("11-2022-21T15:08:53.282Z", Timestamp.class),
                "Timestamp format exception, expected format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        );
    }

    @Test
    public void throwsValueRejectedExceptionForUnparseableTimestamp_unnecessaryAdditionalCharacters() {
        assertThrows(
                ValueRejectedException.class,
                () -> converterWithDefaultFormats.convert("2022-11-21T15:08:53.282Z-invalid-format", Timestamp.class),
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
    public void throwsValueRejectedExceptionForUnparseableTimestampAndCustomFormat_differentThanExpectedDateFormat() {
        Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSMM-dd'T'", EMPTY_RESULT, null);

        assertThrows(
                ValueRejectedException.class,
                () -> converterWithCustomFormat.convert("15-2022:08:53.28206-16T", Timestamp.class),
                "Timestamp format exception, expected format: yyyy-HH:mm:ss.SSSMM-dd'T'"
        );

    }

    @Test
    public void throwsValueRejectedExceptionForUnparseableTimestampAndCustomFormat_unnecessaryAdditionalCharacters() {
        Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSMM-dd'T'", EMPTY_RESULT, null);

        assertThrows(
                ValueRejectedException.class,
                () -> converterWithCustomFormat.convert("2020-15:08:53.28206-16T-invalid-format", Timestamp.class),
                "Timestamp format exception, expected format: yyyy-HH:mm:ss.SSSMM-dd'T'"
        );
    }

    @Test
    public void appendsDefaultTimeDuringConversionIfConverterHasOnlyDateFormatSpecified() {
        Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EMPTY_RESULT, null);
        Timestamp timestamp = converterWithCustomFormat.convert("2022-12-13", Timestamp.class);

        assertThat(timestamp)
                .hasYear(2022)
                .hasMonth(12)
                .hasDayOfMonth(13)
                .hasHourOfDay(0)
                .hasMinute(0)
                .hasSecond(0)
                .hasMillisecond(0);
    }
}
