/*
 * Copyright 2014-2025 the original author or authors.
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

import jakarta.persistence.criteria.Root;

import java.util.Objects;

/**
 * This is a pair of alias (text from query) and query Root.
 *
 * Equals uses identity comparison for Root part. Why? Because in Hibernate 6 2 roots from 2 different queries
 * may yield true in equals. This caused problems with paginated queries: join was evaluated and cached but
 * then Hibernate threw exception on attempt to reuse cached join in count query. It used to work in previous versions...
 *
 * Identity comparison seems to fix that. I don't like this solution, but I don't see any other for now.
 *
 * @author Tomasz Kaczmarzyk
 */
public class Alias {

    private String alias;
    private Root<?> queryRoot;

    public Alias(String alias, Root<?> queryRoot) {
        this.alias = alias;
        this.queryRoot = queryRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alias alias1 = (Alias) o;
        return Objects.equals(alias, alias1.alias) && queryRoot == alias1.queryRoot; // identity comparison for Root!
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, queryRoot);
    }

    public static Alias of(String alias, Root<?> queryRoot) {
        return new Alias(alias, queryRoot);
    }
}
