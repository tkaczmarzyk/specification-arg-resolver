package net.kaczmarzyk.spring.data.jpa.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.criteria.JoinType;

/**
 * Specifies a join part of a query, e.g. {@code select c from Customer c inner join c.addresses a}
 *
 * @author Tomasz Kaczmarzyk
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.TYPE })
public @interface Join {

	/**
	 * Specifies a collection property to join on, e.g. "addresses"
	 */
	String on();
	
	/**
	 * Specifies an alias for the joined part, e.g. "a"
	 */
	String alias();
	
	/**
	 * Whether the query should return distinct results or not
	 */
	boolean distinct() default true;
	
	JoinType type() default JoinType.INNER;
}
