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
package net.kaczmarzyk.spring.data.jpa.domain;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.data.jpa.domain.Specification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;


/**
 * @author Tomasz Kaczmarzyk
 */
public abstract class PathSpecification<T> implements Specification<T> {
    
    protected String path;

    public PathSpecification(String path) {
        this.path = path;
    }
    
    protected <F> Path<F> path(Root<T> root) {
        LinkedList<FieldAndPath<F>> paths = paths(root);
        return paths.getLast().getPath();
    }

    @SuppressWarnings("unchecked")
    private <F> LinkedList<FieldAndPath<F>> paths(Root<T> root) {
        LinkedList<FieldAndPath<F>> paths = new LinkedList<>();
        Path<?> expr = null;
        for (String field : path.split("\\.")) {
          FieldAndPath<F> fp;
          if (expr == null) {
            fp = new FieldAndPath<>(field, (Path<F>) root);
          } else {
            fp = new FieldAndPath<>(field, (Path<F>) expr);
          }
          paths.add(fp);
          expr = fp.getPath();
        }
        return paths;
    }
    
    protected Class<?> javaType(Root<T> root) {
      FieldAndPath<?> path = paths(root).getLast();
      return path.getIndicatedJavaType().orElse(path.getPath().getJavaType());
    }
  
    private class FieldAndPath<X> {

      private final String field;
      private final Path<X> original;
      private final Path<X> path;

      FieldAndPath(String field, Path<X> original) {
        super();
        this.field = Objects.requireNonNull(field);
        this.original = Objects.requireNonNull(original);
        this.path = original.get(field);
      }

      Path<X> getPath() {
        return path;
      }

      Optional<Class<?>> getIndicatedJavaType() {
        String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        Optional<AnnotatedElement> indicated = Optional
            .ofNullable(MethodUtils.getMatchingAccessibleMethod(original.getJavaType(), methodName));

        if (!indicated.isPresent()) {
          indicated = Optional.ofNullable(FieldUtils.getField(original.getJavaType(), field, true));
        }

        return indicated.map(f -> f.getAnnotation(JsonSubTypes.class))
            .map(JsonSubTypes::value)
            .map(types -> types[0]) // always get the first type in array
            .map(Type::value);
      }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        PathSpecification<?> other = (PathSpecification<?>) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}
