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

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonBodyParams implements BodyParams {

	private static final String DOT_DELIMITER = "\\.";

	private final JsonElement requestBody;

	private JsonBodyParams(JsonElement requestBody) {
		this.requestBody = requestBody;
	}

	public static JsonBodyParams parse(String requestBody) {
		return new JsonBodyParams(JsonParser.parseString(requestBody));
	}

	@Override
	public Collection<String> getParamValues(String paramKey) {
		JsonElement value = getElementByKey(requestBody, paramKey);
		return value != null ? getJsonElementValues(value) : Collections.emptyList();
	}

	private static JsonElement getElementByKey(JsonElement rootElement, String key) {
		String[] keyParts = key.split(DOT_DELIMITER, 2);
		if (!rootElement.isJsonObject()) {
			throw new JsonParseException("Failed parse JSON node with key " + keyParts[0] + ". Should be JSON object");
		}

		boolean isLastKey = keyParts.length == 1;
		JsonElement childElement = rootElement.getAsJsonObject().get(keyParts[0]);
		return isLastKey ? childElement : getElementByKey(childElement, keyParts[1]);
	}

	private static Collection<String> getJsonElementValues(JsonElement jsonElement) {
		if (jsonElement.isJsonObject()) {
			throw new JsonParseException("Value by key should be primitive or array primitives. Found: " + jsonElement);
		} else if (jsonElement.isJsonArray()) {
			return getJsonArrayValues(jsonElement.getAsJsonArray());
		} else if (jsonElement.isJsonPrimitive()) {
			return Collections.singletonList(jsonElement.getAsJsonPrimitive().getAsString());
		}
		return Collections.emptyList();
	}

	private static Collection<String> getJsonArrayValues(JsonArray jsonArray) {
		List<String> result = new ArrayList<>();
		for (JsonElement element : jsonArray) {
			if (!element.isJsonPrimitive()) {
				throw new IllegalArgumentException("Array by key contains not primitives. Array: " + jsonArray);
			}
			result.add(element.getAsJsonPrimitive().getAsString());
		}
		return result;
	}
}
