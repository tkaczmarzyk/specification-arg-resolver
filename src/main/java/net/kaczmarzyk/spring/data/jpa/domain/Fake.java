package net.kaczmarzyk.spring.data.jpa.domain;

/**
 * Marker interface to tell that a Specification is not a real filter, but rather some logic to modify the query.
 * An example is {@link Join} which creates join objects which might be used by another specs
 *
 * @author Tomasz Kaczmarzyk
 */
interface Fake {

}
