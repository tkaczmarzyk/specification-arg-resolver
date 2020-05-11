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

import java.text.ParseException;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;


/**
 * Filters with {@code path < date} where-clause.
 * 
 * @deprecated Consider using {@link net.kaczmarzyk.spring.data.jpa.domain.LessThan LessThan}
 * 
 * @author Tomasz Kaczmarzyk
 */
@Deprecated
public class DateBefore<T> extends DateSpecification<T> {

	private static final long serialVersionUID = 1L;
	
	private Date date;

    public DateBefore(QueryContext queryContext, String path, String[] args, Converter converter) throws ParseException {
        super(queryContext, path, args, converter);
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("expected a single http-param, but was: " + args);
        }
        String dateStr = args[0];
        this.date = converter.convertToDate(dateStr);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.lessThan(this.<Date>path(root), date);
    }

}
