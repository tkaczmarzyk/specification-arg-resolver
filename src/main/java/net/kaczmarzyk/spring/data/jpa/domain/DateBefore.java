/**
 * Copyright 2014 the original author or authors.
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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * Filters with {@code path < date} where-clause.
 * 
 * @author Tomasz Kaczmarzyk
 */
public class DateBefore<T> extends PathSpecification<T> {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private Date date;

    public DateBefore(String path, String[] args, String[] config) throws ParseException {
        super(path);
        if (args == null || args.length != 1 || (config != null && config.length != 1)) {
            throw new IllegalArgumentException();
        }
        String pattern = DEFAULT_DATE_FORMAT;
        if (config != null) {
            pattern = config[0];
        }
        String dateStr = args[0];
        this.date = new SimpleDateFormat(pattern).parse(dateStr);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.lessThan(this.<Date>path(root), date);
    }

}
