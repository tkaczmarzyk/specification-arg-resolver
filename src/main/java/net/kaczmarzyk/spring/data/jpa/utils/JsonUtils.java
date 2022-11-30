/**
 * Copyright 2014-2020 the original author or authors.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Shakarov
 */
public class JsonUtils {
    private static final String DOT_DELIMITER = "\\.";

    private JsonUtils() {
    }

    public static Collection<String> getValuesFromJson(JsonElement json, String key) {
        JsonElement value = getElementByKey(json, key);
        return value != null ? getJsonElementValues(value) : Collections.emptyList();
    }

    private static JsonElement getElementByKey(JsonElement rootElement, String key) {
        if (rootElement == null) return JsonNull.INSTANCE;
        String[] keyParts = key.split(DOT_DELIMITER, 2);
        if (!rootElement.isJsonObject()) {
            throw new JsonParseException("Failed parse JSON node with key " + keyParts[0] + ". Should be JSON object");
        }

        boolean isLastKey = keyParts.length == 1;
        JsonElement parentElement = rootElement.getAsJsonObject().get(keyParts[0]);
        return isLastKey ? parentElement : getElementByKey(parentElement, keyParts[1]);
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
            if (!element.isJsonPrimitive())
                throw new IllegalArgumentException("Array by key contains not primitives. Array: " + jsonArray);
            result.add(element.getAsJsonPrimitive().getAsString());
        }
        return result;
    }

}
