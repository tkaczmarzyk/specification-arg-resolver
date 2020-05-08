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
package net.kaczmarzyk.spring.data.jpa.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;


/**
 * Filters for entities with path equal to any of passed enum constant names,
 * regardless of the (chosen by user) way of persisting enums in the database.
 * 
 * @author Maciej Szewczyszyn
 * 
 * @deprecated use {@link net.kaczmarzyk.spring.data.jpa.domain.Equal Equal}
 *  or {@link net.kaczmarzyk.spring.data.jpa.domain.In In} specifications that handle enums and other data types
 */
@Deprecated
public class EqualEnum<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;
	
	protected String[] searchedNames;

    public EqualEnum(QueryContext queryContext, String path, String... args) {
        super(queryContext, path);
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Expected at least one argument (the enum constant name to match against)");
        } else {
            this.searchedNames = args;
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Enum<?>> matchingEnumConstants = findMatchingEnumConstants(getEnumClass(root));
        Iterator<Enum<?>> iterator = matchingEnumConstants.iterator();
        Predicate combinedPredicates = builder.equal(this.<Enum<?>> path(root), iterator.next());
        while (iterator.hasNext()) {
            combinedPredicates = builder.or(builder.equal(this.<Enum<?>> path(root), iterator.next()), combinedPredicates);
        }
        return combinedPredicates;
    }

    private Class<? extends Enum<?>> getEnumClass(Root<T> root) {
        Class<? extends Enum<?>> enumClass = this.<Enum<?>> path(root).getJavaType();
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Type of field with path " + super.path + " is not enum!");
        }
        return enumClass;
    }

    private List<Enum<?>> findMatchingEnumConstants(Class<? extends Enum<?>> enumClass) {
        List<String> searchedNamesCopy = new ArrayList<>(Arrays.asList(searchedNames));
        List<Enum<?>> matchingEnumConstants = new ArrayList<>();
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        for (Enum<?> enumConstant : enumConstants) {
            Iterator<String> i = searchedNamesCopy.iterator();
            while (i.hasNext()) {
                if (enumConstant.name().equals(i.next())) {
                    matchingEnumConstants.add(enumConstant);
                    i.remove();
                }
            }
        }
        if (searchedNamesCopy.size() > 0) {
            throw new IllegalArgumentException("The following enum constants do not exists: " + StringUtils.join(searchedNamesCopy, ", "));
        }
        return matchingEnumConstants;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(searchedNames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EqualEnum<?> other = (EqualEnum<?>) obj;
        if (!Arrays.equals(searchedNames, other.searchedNames))
            return false;
        return true;
    }

}