package net.kaczmarzyk.spring.data.jpa.utils;

/**
 * Ugly way to share context between different specifications -- e.g. joins (see {@link JoinSpecificationResolver})
 *
 * @author Tomasz Kaczmarzyk
 */
public interface QueryContext {

	Object get(String key);
	
	void put(String key, Object value);
}
