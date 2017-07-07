v0.9.2
======

* introduced `NotNull` specification

v0.9.1
======

* bumped dependencies to the latest Spring Boot version and JPA 2.1 API

v0.9.0
======

* added `EqualIgnoreCase` specification
* introduced `Null` specification which accepts a boolean HTTP param to dynamically add `is null` or `is not null` part to the query
* introduced `onTypeMismatch` property of `@Spec` to define whether an exception should be thrown or empty result returned when an invalid value is passed (e.g. a non numeric value while field type is `Integer`). Default behaviour is to return an empty result, which __is a breaking change__ (an exception was thrown in previous versions). Use `onTypeMismatch=EXCEPTION` to match old behaviour.

v0.8.0
======

* fixed stack overflow issue with annotated interfaces!
* added `GreaterThan`, `GreaterThanOrEqual`, `LessThan`, `LessThanOrEqual` specs
* `DateAfter`, `DateBefore` and their invlusive versions are now deprecated (use the above specs)

v0.7.0
======

* added `@JoinFetch` and `@Joins` (see README.md for the details)

v0.6.1
======

* added date inclusive specs

v0.6.0
======

* it is now allowed to annotate a custom interface that extends `Specification`, eg.:

  ```java
  @Or({
      @Spec(path="firstName", params="name", spec=Like.class),
      @Spec(path="lastName", params="name", spec=Like.class)
  })
  public interface FullNameSpec extends Specification<Customer> {
  }
  ```

  it can be then used as controller parameter without further annotations, i.e.:

  ```java
  @RequestMapping("/customers")
  @ResponseBody
  public Object findByFullName(FullNameSpec spec) {
      return repository.findAll(spec);
  }
  ```
* added optional `constVal` attribute in `@Spec`. It allows to define a constant part of the query that does not use any HTTP parameters, e.g.:
  
  ```java
  @And({
      @Spec(path="deleted", spec=Equal.class, constVal="false"),
	  @Spec(path="firstName", spec=Like.class)
  })
  ```
  for handling requests such as `GET /customers?firstName=Homer` and executing queries such as: `select c from Customer c where c.firstName like %Homer% and c.deleted = false`
* it is possible to combine parameter and interface annotations. They are combined with 'and' operator. For example you can create a basic interface like:

  ```java
  @Spec(path="deleted", spec=Equal.class, constVal="false")
  public interface NotDeletedEntitySpec<T> extends Specification<T> {}
  ```

  And use it for your queries in the controller:

  ```java
  @RequestMapping("/customers")
  @ResponseBody
  public Object findNotDeletedCustomerByLastName(
              @Spec(path="lastName", spec=Equal.class) NotDeletedEntitySpec<Customer> spec) {

      return repository.findAll(spec);
  }
  ```
* `Equal` and `In` now support boolean values correctly
* introduced `IsNull` specification
* introduced `@Conjunction` and `@Disjunction` for nesting ands and ors within each other

v0.5.0
======

* introduced `DateAfter` specification
* introduced `Equal` that supports exact match for numbers, strings, dates and enums
* introduced `In` that supports in operator for numbers, strings, dates and enums
* deprecated `EqualEnum` (use `Equal` and `In`)
