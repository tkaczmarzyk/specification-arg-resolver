/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web.annotation;

import jakarta.persistence.criteria.JoinType;
import java.lang.annotation.*;


/**
 * Specifies paths to be join-fetched in the query
 * 
 * Can be repeated, to specify multiple fetches.
 * 
 * @author Tomasz Kaczmarzyk
 * @author Gerald Humphries
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.TYPE })
@Repeatable(RepeatedJoinFetch.class)
public @interface JoinFetch {

    String[] paths();

    String alias() default "";
    
    JoinType joinType() default JoinType.LEFT;

    /**
     * Hibernate since version 6.0 deduplicates results (https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#query-sqm-distinct).
     * For the paged and count queries the distinct should be set to true (default behaviour) in main cases.
     *
     * Changing distinct to false (when using Hibernate) makes sense only in count queries -- in all other cases it will lead to unexpected behaviour.
     */
    boolean distinct() default true;
}
