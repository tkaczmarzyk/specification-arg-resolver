/**
 * Copyright 2014-2015 the original author or authors.
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

import java.text.ParseException;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * Filters with {@code path between arg1 and arg2} where-clause.
 * 
 * @author Tomasz Kaczmarzyk
 */
public class DateBetween<T> extends DateSpecification<T> {

    private Date after;
    private Date before;
    
    public DateBetween(String path, String... args) throws ParseException {
        this(path, args, null);
    }

    public DateBetween(String path, String[] args, String[] config) throws ParseException {
        super(path, args, config);
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("expected 2 http params (date boundaries), but was: " + args);
        }
        String afterDateStr = args[0];
        String beforeDateStr = args[1];
        this.after = format.parse(afterDateStr);
        this.before = format.parse(beforeDateStr);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.between(this.<Date>path(root), after, before);
    }
}