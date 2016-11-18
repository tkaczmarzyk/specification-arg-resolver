specification-arg-resolver
==========================

An alternative API for filtering data with Spring MVC &amp; Spring Data JPA.

A thorough introduction and the original rationale behind this component can be found my blog: http://blog.kaczmarzyk.net/2014/03/23/alternative-api-for-filtering-data-with-spring-mvc-and-spring-data/. In this file you can find a summary of all the current features and some API examples.

You can also take a look on a working Spring Boot app that uses this library: https://github.com/tkaczmarzyk/specification-arg-resolver-example.

Basic usage
-----------

The following HTTP request:

```
GET http://myhost/api/customers?firstName=Homer
```

can be handled with the following controller method:

```java

@RequestMapping(value = "/customers", params = "firstName")
public Iterable<Customer> findByFirstName(  
      @Spec(path = "firstName", spec = Like.class) Specification<Customer> spec) {

    return customerRepo.findAll(spec);
}
```

which will result in the following JPA query:

```sql
select c from Customer c where c.firstName like '%Homer%'
```

Alternatively you can annotate an interface:

```java
  @Spec(path="firstName", params="name", spec=Like.class)
  public interface NameSpec extends Specification<Customer> {
  }
```

and then use it as a controller parameter without any further annotations.

### Enabling spec annotations in your Spring app ###

All you need to do is to wire `SpecificationArgumentResolver` into your application. Then you can use `@Spec` and other annotations in your controllers. `SpecificationArgumentResolver` implements Spring's `HandlerMethodArgumentResolver` and can be plugged in as follows:

```java

@Configuration
@EnableJpaRepositories
public class MyConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver());
    }

    ...
}
```

### Fully controlled converter

`Converter` converts http parameter to object via Jackson. And you can determine your converting logic by passing your custom `Converter` into `SpecificationArgumentResolver`:

```java
ObjectMapper myMapper = new ObjectMapper();
myMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
new SpecificationArgumentResolver(new Converter(myMapper));
```

and then `@Spec` now supports JSR310 conversion, ex: `java.time.LocalDate`, `java.time.LocalDateTime`...

### Indicate field type

Use `@com.fasterxml.jackson.annotation.JsonSubTypes` to indicate the specify type of field, and it will be used by `Converter` to do the converting. It will help to get the correct java type, especially generic fields in super class.

We somethings declare `id` field in super class as generic type.

```java
@MappedSuperclass
public abstract class Generic<ID extends Serializable> {
  @Id
  @GeneratedValue
  private ID id; 
}
```

But generic type can not be recognized correctly in runtime, therefore we have to indicate the type.

```java
@Entity
public class MyEntity extends Generic<Long>{
	
	@JsonSubTypes(@Type(Long.class))
	public Long getId() {
		return super.getId();
	}
    
	// more fields
}
```

It will try to find `@JsonSubTypes` on getter methods first, and then fields.

Simple specifications
----------------------

Use `@Spec` annotation to automatically resolve a `Specification` argument of your controller method. `@Spec` has `path` property that should be used to specify property path of the attribute of an entity, e.g. `address.city`. By default it's also the name of the expected HTTP parameter, e.g. `GET http://myhost?address.city=Springfield`.

Use `spec` attribute of the annotation to specify one of the following strategies for filtering.

### Like ###

Filters using JPAQL `like` expression. It adds a wildcard `%` at the beginning and the end of the actual value, e.g. `(..) where firstName like %Homer%`.

Usage: `@Spec(path="firstName", spec=Like.class)`.

### LikeIgnoreCase  ###

Works as `Like`, but the query is also case-insensitive.

Usage: `@Spec(path="firstName", spec=LikeIgnoreCase.class)`.

### Equal ###

Compares an attribute of an entity with the value of a HTTP parameter (exact match). E.g. `(..) where gender = FEMALE`.

Supports multiple data types: numbers, booleans, strings, dates, enums.

Usage: `@Spec(path="gender", spec=Enum.class)`.

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overriden with a configuration parameter (see `LessThan` below).

### EqualIgnoreCase ###

Works as `Equal`, but the query is also case-insensitive.

### In ###

Compares an attribute of an entity with multiple values of a HTTP parameter. E.g. `(..) where gender in (MALE, FEMALE)`.

HTTP request example:

    GET http://myhost/customers?gender=MALE&gender=FEMALE

Supports multiple data types: numbers, booleans, strings, dates, enums.

Usage: `@Spec(path="gender", spec=In.class)`.

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overriden with a configuration parameter (see `LessThan` below).

### Null ###

Filters using `is null` or `is not null`, depending on the value of the parameter passed in. A value of `true` will filter for `is null`, and a value of `false` will filter for `is not null`.

The data type of the field specified in `path` can be anything, but the HTTP parameter must be a Boolean. You should use `params` attribute to make it clear that the parameter is filtering for null values.

Usage: `@Spec(path="activationDate", params="activationDateNull" spec=Null.class)`.

If you want the query to be static, i.e. not depend on any HTTP param, use `constVal` attribute of `Spec` annotation:

For example `@Spec(path="nickname", spec=Null.class, constVal="true")` will always add `nickname is null` to the query.

### GreaterThan, GreaterThanOrEqual, LessThan, LessThanOrEqual ###

Filters using a comparison operator (`>`, `>=`, `<` or `<=`). Supports multiple field types: strings, numbers, booleans, enums, dates. Field types must be Comparable (e.g, implement the Comparable interface); this is a JPA constraint.

Usage: `@Spec(path="creationDate", spec=LessThan.class)`.

For temporal values, the default date format is `yyyy-MM-dd`. You can override it by providing a config value to the annotation: `@Spec(path="creationDate", spec=LessThan.class, config="dd-MM-yyyy")`.

NOTE: comparisons are dependent on the underlying database.
 * Comparisons of floats and doubles (especially floats) may be incorrect due to precision loss.
 * Comparisons of booleans may be dependent on the underlying database representation.
 * Comparisons of enums will be of their ordinal or string representations, depending on what you specified to JPA, e.g., `@Enumerated(EnumType.STRING)`, `@Enumerated(EnumType.ORDINAL)` or the default (`@Enumerated(EnumType.ORDINAL)`)

### DateBetween ###

Filters by checking if a temporal field of an entity is in the provided date range. E.g. `(..) where creation date between :after and :before`.

It requires 2 HTTP parameters (for lower and upper bound). You should use `params` attribute of the `@Spec` annotation, i.e.: `@Spec(path="registrationDate", params={"registeredAfter","registeredBefore"}, spec=DateBetween.class)`. The corresponding HTTP query would be: `GET http://myhost/customers?registeredAfter=2014-01-01&registeredBefore=2014-12-31`.

You can configure the date pattern as with `LessThan` described above.

Join fetch
----------

You can use `@JoinFetch` annotation to specify paths to perform fetch join on. For example:

```java
@RequestMapping("/customers")
public Object findByCityFetchOrdersAndAddresses(
        @JoinFetch(paths = { "orders", "addresses" })
        @Spec(path="address.city", params="town", spec=Like.class) Specification<Customer> customersByCitySpec) {

    return customerRepo.findAll(customersByCitySpec);
}
```

The default join type is `LEFT`. You can use `joinType` attribute of the annotation to specify different value. You can specify multiple different joins with container annotation `@Joins`, for example:

```java
@RequestMapping("/customers")
public Object findByCityFetchOrdersAndAddresses(
        @Joins({
            @JoinFetch(paths = "orders")
            @JoinFetch(paths = "addresses", joinType = JoinType.INNER)
        })
        @Spec(path="address.city", params="town", spec=Like.class) Specification<Customer> customersByCitySpec) {

    return customerRepo.findAll(customersByCitySpec);
}
```

You can use join annotations with custom specification interfaces (see below).


Advanced HTTP parameter handling
--------------------------------

### Handling non-present HTTP parameters ###

If the HTTP parameter is not present, the resolved `Specification` will be `null`. It means no filtering at all when passed to a repository. If you want to make the parameter non-optional, you should use standard Spring MVC annotations, e.g. `@RequestMapping(params={"firstName"})`.

### Mapping HTTP parameter name to property path of an entity ###

By default, the expected HTTP parameter is the same as the property path. If you want them to differ, you can use `params` attribute of `@Spec`. For example this method:

```java
@RequestMapping("/customers")
public Object findByCity(
        @Spec(path="address.city", params="town", spec=Like.class) Specification<Customer> customersByCitySpec) {

    return customerRepo.findAll(customersByCitySpec);
}
```

will handle `GET http://myhost/customers?town=Springfield` as `select c from Customer c where city.address like '%Springfield%'`.

Static parts of queries
-----------------------

If you don't want to bind your Specification to any HTTP parameter, you can use `constVal` attribute of `@Spec`. For example:

```java
@Spec(path="deleted", spec=Equal.class, constVal="false")
```

will alwas produce the following: `where deleted = false`. It is often convenient to combine such a static part with dynamic ones using `@And` or `@Or` described below.

Required value of queries
-----------------------

If you prefer an exception being thrown if the parameter is missing in the request, you can use `required` attribute of `@Spec`. For example:

```java
@Spec(path="role", spec=Equal.class, required=true) // defaults to false
```

Alternatively, provide a `constVal ` or `defaultVal`, which implicitly sets this flag to `false`.

Default value of queries
-----------------------

The default value to use as a fallback when the request parameter is not provided or has an empty value.

```java
@Spec(path="role", spec=Equal.class, defaultVal="USER")
```

Supplying `constVal` implicitly sets `defaultVal` to empty.

Combining specs
---------------

You can combine the specs described above with `or` & `and`. Remember that by default all of the HTTP params are optional. If you want to make all parts of your query required, you must state that explicitly in `@RequestMapping` annotation (see above).

### @And ###

Usage:

```java
@RequestMapping("/customers")
public Object findByName(
        @And({
            @Spec(path="registrationDate", params="registeredBefore", spec=DateBefore.class),
            @Spec(path="lastName", spec=Like.class)}) Specification<Customer> customerSpec) {

    return customerRepo.findAll(customerSpec);
}
```

would handle requests like `GET http://myhost/customers?registeredBefore=2015-01-18&lastName=Simpson`

and execute queries like: `select c from Customer c where c.registrationDate < :registeredBefore and c.lastName like '%Simpson%'`.


### @Or ###

Usage:

```java
@RequestMapping("/customers")
public Object findByName(
        @Or(
            @Spec(path="firstName", params="name", spec=Like.class),
            @Spec(path="lastName", params="name", spec=Like.class)) Specification<Customer> customerNameSpec) {

    return customerRepo.findAll(customerNameSpec);
}
```

would handle requests like `GET http://myhost/customers?name=Mo`

and execute queries like: `select c from Customer c where c.firstName like '%Mo%' or c.lastName like '%Mo'`.

### Nested conjunctions and disjunctions ###

You can put multiple `@And` inside `@Disjunction` or multiple `@Or` inside `@Conjunction`. `@Disjunction` joins nested `@And` queries with 'or' operator. `@Conjunction` joins nested `@Or` queries with 'and' operator. For example:

```java
@RequestMapping("/customers")
public Object findByFullNameAndAddress(
        @Conjunction({
            @Or(@Spec(path="firstName", params="name", spec=Like.class),
                @Spec(path="lastName", params="name", spec=Like.class)),
            @Or(@Spec(path="address.street", params="address", spec=Like.class),
                @Spec(path="address.city", params="address", spec=Like.class))
        }) Specification<Customer> customerSpec) {

    return customerRepo.findAll(customerSpec);
}
```

would handle requests like `GET http://myhost/customers?name=Sim&address=Ever`

and execute queries like `select c from Customer c where (c.firstName like '%Sim%' or c.lastName like '%Sim%') and (c.address.street like '%Ever%' or c.address.city like '%Ever%')`.

You must use `@Conjunction` and `@Disjunction` as top level annotations (instead of regular `@And` and `@Or`) because of limitations of Java annotation syntax (it does not allow cycle in annotation references).

You can join nested `@And` and `@Or` queries with simple `@Spec`, for example:

```java
@RequestMapping("/customers")
public Object findByFullNameAndAddressAndNickName(
        @Conjunction(value = {
            @Or(@Spec(path="firstName", params="name", spec=Like.class),
                @Spec(path="lastName", params="name", spec=Like.class)),
            @Or(@Spec(path="address.street", params="address", spec=Like.class),
                @Spec(path="address.city", params="address", spec=Like.class))
        }, and = @Spec(path="nickName", spec=Like.class) Specification<Customer> customerSpec) {

    return customerRepo.findAll(customerSpec);
}

```

```java
@RequestMapping("/customers")
public Object findByLastNameOrGoldenByFirstName(
        @Disjunction(value = {
            @And({@Spec(path="golden", spec=Equal.class, constVal="true"),
                @Spec(path="firstName", params="name", spec=Like.class)})
        }, or = @Spec(path="lastName", params="name", spec=Like.class) Specification<Customer> customerSpec) {

    return customerRepo.findAll(customerSpec);
}
```

Annotated specification interfaces
----------------------------------

You can annotate a custom interface that extends `Specification`, eg.:

```java
@Or({
    @Spec(path="firstName", params="name", spec=Like.class),
    @Spec(path="lastName", params="name", spec=Like.class)
})
public interface FullNameSpec extends Specification<Customer> {
}
```

It can be then used as a controller parameter without further annotations, i.e.:

```java
@RequestMapping("/customers")
@ResponseBody
public Object findByFullName(FullNameSpec spec) {
    return repository.findAll(spec);
}
```

When such parameter is additionally annotated, the both specifications (from the interface and the parameter annotations) are joined with 'and' operator. For example you can define a base interface like this:

```java
@Spec(path="deleted", spec=Equal.class, constVal="false")
public interface NotDeletedEntitySpec<T> extends Specification<T> {}
```

and then use it as a foundation for you controller as follows:

```java
@RequestMapping("/customers")
@ResponseBody
public Object findNotDeletedCustomerByLastName(
            @Spec(path="lastName", spec=Equal.class) NotDeletedEntitySpec<Customer> spec) {

    return repository.findAll(spec);
}
```

to execute queries such as `select c from Customer c where c.deleted = false and c.lastName like %Homer%`.

Handling different field types
------------------------------

Consider a field `age` of type `Integer` and the following specification definition:

```java
@Spec(path="age", spec=Equal.class)
```

If non-numeric values is passed with the HTTP request (e.g. `?age=test`), then the result list will be empty. If you want an exception to be thrown instead, use `onTypeMismatch` property of the `Spec` annotation, i.e:

```java
@Spec(path="age", spec=Equal.class, onTypeMismatch=OnTypeMismatch.EXCEPTION)
```

This behaviour has changed in version `0.9.0` (exception was the default value in previous ones). The default `OnTypeMismatch.EMPTY_RESULT` is useful when using `@And` or `@Or` and their inner specs refer to fields of different types, e.g.:

```java
@And({
    @Spec(path="firstName", params="query", spec=Equal.class),
    @Spec(path="customerId", params="query", spec=Equal.class)})
```

(assuming that `firstName` is `String` and `customerId` is a numeric type)


Download binary releases
------------------------

Specification argument resolver is available in the Maven Central:

```xml
<dependency>
    <groupId>net.kaczmarzyk</groupId>
    <artifactId>specification-arg-resolver</artifactId>
    <version>0.9.0</version>
</dependency>
```

If a new version is not yet available in the central repository, you can grab it from my private repo:

```xml
<repository>
    <id>kaczmarzyk.net</id>
    <url>http://repo.kaczmarzyk.net</url>
</repository>
```
