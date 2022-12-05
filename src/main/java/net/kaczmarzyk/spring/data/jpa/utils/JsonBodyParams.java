package net.kaczmarzyk.spring.data.jpa.utils;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonBodyParams implements BodyParams {

	private static final String DOT_DELIMITER = "\\.";

	private final JsonElement bodyParamsJsonElement;

	private JsonBodyParams(JsonElement bodyParamsJsonElement) {
		this.bodyParamsJsonElement = bodyParamsJsonElement;
	}

	public static JsonBodyParams parse(String requestBody) {
		return new JsonBodyParams(JsonParser.parseString(requestBody));
	}

	@Override
	public Collection<String> getParamValues(String paramKey) {
		JsonElement value = getElementByKey(bodyParamsJsonElement, paramKey);
		return value != null ? getJsonElementValues(value) : Collections.emptyList();
	}

	private static JsonElement getElementByKey(JsonElement rootElement, String key) {
		if (rootElement == null){
			return JsonNull.INSTANCE;
		}
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
