/**
 * Copyright 2014-2016 the original author or authors.
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

import static java.util.Objects.requireNonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;


/**
 * {@code Converter} converts http parameter to object via Jackson
 * 
 * @author Tomasz Kaczmarzyk
 * @author Matt S.Y. Ho
 */
@SuppressWarnings("unchecked")
public class Converter implements Cloneable {

    public static class ValuesRejectedException extends IllegalArgumentException {
        
        private static final long serialVersionUID = 1L;

        private Collection<String> rejectedValues;
        
        public ValuesRejectedException(Collection<String> rejectedValues, String message) {
            super(message);
            this.rejectedValues = rejectedValues;
        }
        
        public Collection<String> getRejectedValues() {
            return rejectedValues;
        }
        
        @Override
        public String toString() {
            return this.getClass() + ": " + getMessage();
        }
    }
    
    public static class ValueRejectedException extends IllegalArgumentException {
        
        private static final long serialVersionUID = 1L;
        
        private String rejectedValue;
        
        public ValueRejectedException(String rejectedValue, String message) {
          super(message);
          this.rejectedValue = rejectedValue;
        }
      
        public ValueRejectedException(String rejectedValue, String message, Throwable cause) {
            super(message, cause);
            this.rejectedValue = rejectedValue;
        }
        
        public String getRejectedValue() {
            return rejectedValue;
        }
        
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ": " + getMessage();
        }
    }
    
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER;
    static {
      DEFAULT_OBJECT_MAPPER = new ObjectMapper();
      DEFAULT_OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    public static final Converter DEFAULT = new Converter();
    
    private ObjectMapper objectMapper;
    
    public Converter() {
      this(DEFAULT_OBJECT_MAPPER);
    }
    
    public Converter(ObjectMapper objectMapper) {
      super();
      this.objectMapper = requireNonNull(objectMapper, "objectMapper");
    }
    
    public <T> T convert(String value, Class<T> expectedClass) {
        if (expectedClass.isEnum()) {
          return (T) convertToEnum(value, (Class<? extends Enum<?>>) expectedClass);
        }

        try {
          return objectMapper.convertValue(value, expectedClass);
        } catch (IllegalArgumentException e) {
            throw new ValueRejectedException(value, "Conversion fails due to incompatible type", e);
        }
    }

    public <T> List<T> convert(List<String> values, Class<T> expectedClass, OnTypeMismatch onTypeMismatch) {
        if (expectedClass == String.class) {
            return (List<T>) values;
        }
        List<String> rejected = null;
        List<T> result = new ArrayList<>();
        for (String value : values) {
            try {
                result.add(convert(value, expectedClass));
            } catch (ValueRejectedException e) {
                if (rejected == null) {
                    rejected = new ArrayList<>();
                }
                rejected.add(e.getRejectedValue());
            }
        }
        onTypeMismatch.handleRejectedValues(rejected);
        return result;
    }
    
    private <T> T convertToEnum(String value, Class<? extends Enum<?>> enumClass) {
      for (Enum<?> enumVal : enumClass.getEnumConstants()) {
          if (enumVal.name().equals(value)) {
              return (T) enumVal;
          }
      }
      throw new ValueRejectedException(value, "could not find value " + value + " for enum class " + enumClass.getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((objectMapper == null) ? 0 : objectMapper.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Converter other = (Converter) obj;
        if (objectMapper == null) {
            if (other.objectMapper != null)
                return false;
        } else if (!objectMapper.equals(other.objectMapper))
            return false;
        return true;
    }
    
    public ObjectMapper getObjectMapper() {
      return objectMapper;
    }

    @Override
    public Converter clone() throws CloneNotSupportedException {
      return new Converter(new ObjectMapper(objectMapper.getFactory()));
    }
    
}
