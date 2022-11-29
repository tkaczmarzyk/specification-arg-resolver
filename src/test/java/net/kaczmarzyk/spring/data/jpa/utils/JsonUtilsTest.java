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

import com.google.gson.JsonParseException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

public class JsonUtilsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void getSingleValueByNotCompositeKeyWhenValueIsString() {
        String json = "{ \"key\": \"value\" }";
        Collection<String> result = JsonUtils.getValuesFromJson(json, "key");

        Assertions.assertThat(result).containsExactly("value");
    }

    @Test
    public void getSingleValueByNotCompositeKeyWhenValueIsNumber() {
        String json = "{ \"key\": 2 }";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "key");

        Assertions.assertThat(result).containsExactly("2");
    }

    @Test
    public void getSingleValueByNotCompositeKeyWhenValueIsBoolean() {
        String json = "{ \"key\": true }";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "key");

        Assertions.assertThat(result).containsExactly("true");
    }

    @Test
    public void getSingleValueByNotCompositeKeyWhenValueIsNull() {
        String json = "{ \"key\": null }";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "key");

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getSingleValueWhenValueIsObject() {
        String json = "{ \"key\": { \"innerKey\": \"value\"}}";

        exceptionRule.expect(JsonParseException.class);
        exceptionRule.expectMessage("Value by key should be primitive or array primitives");

        JsonUtils.getValuesFromJson(json, "key");
    }

    @Test
    public void getMultipleValuesByNotCompositeKey() {
        String json = "{ \"array\": [\"value1\", \"value2\"] }";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "array");

        Assertions.assertThat(result).containsExactly("value1", "value2");
    }

    @Test
    public void getSingleValueByCompositeKey() {
        String json = "{ \"key1\": { \"key2\": \"value\" }}";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "key1.key2");

        Assertions.assertThat(result).containsExactly("value");
    }

    @Test
    public void getValueFromJsonWhenKeyNotPresent() {
        String json = "{ \"key1\": { \"key2\": \"value\" }}";

        Collection<String> result = JsonUtils.getValuesFromJson(json, "key1.innerKey");

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getValueFromJsonWhenMiddleNodeIsArray() {
        String json = "{ \"key\": { \"array\": [{ \"object1\": \"value1\" }, { \"object2\": \"value2\" }]}}";

        exceptionRule.expect(JsonParseException.class);
        exceptionRule.expectMessage("Should be JSON object");

        JsonUtils.getValuesFromJson(json, "key.array.object1");
    }

    @Test
    public void getValueFromJsonWhenResultArrayContainsObjects() {
        String json = "{ \"key\": { \"array\": [{ \"object1\": \"value1\" }, { \"object2\": \"value2\" }]}}";

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Array by key contains not primitives");

        JsonUtils.getValuesFromJson(json, "key.array");
    }
}