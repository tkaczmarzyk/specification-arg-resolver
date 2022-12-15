/**
 * Copyright 2014-2022 the original author or authors.
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

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.kaczmarzyk.utils.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class JsonBodyParamsTest {

	@Test
	public void parsesJsonToBodyParams() {
		//given
		String json = "{ \"key\": { \"array\": [\"value1\",\"value2\"]}}";

		//when
		JsonBodyParams bodyParams = JsonBodyParams.parse(json);

		//then
		assertThat(bodyParams.getParamValues("key.array"))
				.containsExactly("value1", "value2");
	}

	@Test
	public void throwsJsonSyntaxExceptionWhileParsingInvalidJson() {
		//given
		String json = "{\"invalidJson: ";

		//when-then
		try {
			JsonBodyParams.parse(json);
			fail("expected JsonSyntaxException");
		} catch (JsonSyntaxException exception) {
			// pass
		}
	}

	@Test
	public void returnsSingleValueByNotCompositeKeyWhenValueIsString() {
		//given
		String json = "{ \"key\": \"value\" }";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key");

		//then
		assertThat(result).containsExactly("value");
	}

	@Test
	public void returnsSingleValueByNotCompositeKeyWhenValueIsNumber() {
		//given
		String json = "{ \"key\": 2 }";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key");

		//then
		assertThat(result).containsExactly("2");
	}

	@Test
	public void returnsSingleValueByNotCompositeKeyWhenValueIsBoolean() {
		//given
		String json = "{ \"key\": true }";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key");

		//then
		assertThat(result).containsExactly("true");
	}

	@Test
	public void returnsSingleValueByNotCompositeKeyWhenValueIsNull() {
		//given
		String json = "{ \"key\": null }";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key");

		//then
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void throwsJsonParseExceptionWhenTryingToGetValueFromJsonWhenValueIsObject() {
		//given
		String json = "{ \"key\": { \"innerKey\": \"value\"}}";

		//when-then
		try {
			JsonBodyParams.parse(json).getParamValues("key");
			fail("expected JsonParseException");
		} catch (JsonParseException exception) {
			assertThat(exception)
				.isInstanceOf(JsonParseException.class)
				.hasMessageContaining("Value by key should be primitive or array primitives");
		}
	}

	@Test
	public void returnsMultipleValuesByNotCompositeKey() {
		//given
		String json = "{ \"array\": [\"value1\", \"value2\"] }";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("array");

		//then
		assertThat(result).containsExactly("value1", "value2");
	}

	@Test
	public void returnsSingleValueByCompositeKey() {
		//given
		String json = "{ \"key1\": { \"key2\": \"value\" }}";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key1.key2");

		//then
		assertThat(result).containsExactly("value");
	}

	@Test
	public void returnsEmptyValueFromJsonWhenKeyNotPresent() {
		//given
		String json = "{ \"key1\": { \"key2\": \"value\" }}";

		//when
		Collection<String> result = JsonBodyParams.parse(json).getParamValues("key1.innerKey");

		//then
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void returnsEmptyValueWhenRequestBodyIsNull() {
		//given
		String json = "{ \"key1\": { \"key2\": \"value\" }}";
		MockedStatic<JsonParser> jsonParserMockedStatic = mockStatic(JsonParser.class);

		//when
		jsonParserMockedStatic.when((MockedStatic.Verification) JsonParser.parseString(json)).thenReturn(null);
		JsonBodyParams jsonBodyParams = JsonBodyParams.parse(json);
		Collection<String> result = jsonBodyParams.getParamValues("key1.innerKey");

		//then
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void throwsJsonParseExceptionWhenTryingToGetValueFromJsonWhenMiddleNodeIsArray() {
		//given
		String json = "{ \"key\": { \"array\": [{ \"object1\": \"value1\" }, { \"object2\": \"value2\" }]}}";

		//when-then
		try {
			JsonBodyParams.parse(json).getParamValues("key.array.object1");
			fail("expected JsonParseException");
		} catch (JsonParseException exception) {
			assertThat(exception)
				.isInstanceOf(JsonParseException.class)
				.hasMessageContaining("Should be JSON object");
		}
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenTryingToGetValueFromJsonWhenResultArrayContainsObjects() {
		//given
		String json = "{ \"key\": { \"array\": [{ \"object1\": \"value1\" }, { \"object2\": \"value2\" }]}}";

		//when
		try {
			JsonBodyParams.parse(json).getParamValues("key.array");
			fail("expected JsonParseException");
		} catch (IllegalArgumentException exception) {
			assertThat(exception)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Array by key contains not primitives");
		}
	}
}