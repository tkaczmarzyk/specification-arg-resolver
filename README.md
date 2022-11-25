specification-arg-resolver
==========================
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/donate/?business=9GTMPKJ5X83US&no_recurring=1&item_name=Maintaining+specification-arg-resolver+library&currency_code=USD)

An alternative API for filtering data with Spring MVC &amp; Spring Data JPA.

A thorough introduction and the original rationale behind this component can be found my blog: http://blog.kaczmarzyk.net/2014/03/23/alternative-api-for-filtering-data-with-spring-mvc-and-spring-data/. In this file you can find a summary of all the current features and some API examples.

You can also take a look on a working Spring Boot app that uses this library: https://github.com/tkaczmarzyk/specification-arg-resolver-example.

* Table of contents 
   * [Basic usage](#basic-usage) -- quick start with the lib
      * [Enabling spec annotations in your Spring app](#enabling-spec-annotations-in-your-spring-app)
   * [Simple specifications](#simple-specifications) -- basic specs, such as `Equal`, `Like`, `GreaterThan` etc.
   * [Combining specs](#combining-specs)
      * [@And](#and) -- combining simple specs with `and` keyword
      * [@Or](#or) -- combining simple specs with `or` keyword
      * [Nested conjunctions and disjunctions](#nested-conjunctions-and-disjunctions)
   * [Join](#join) -- filtering by attributes of joined entities
   * [Join fetch](#join-fetch) -- initializing lazy associations
   * [Advanced HTTP parameter handling](#advanced-http-parameter-handling)
      * [Handling non-present HTTP parameters](#handling-non-present-http-parameters)
      * [Mapping HTTP parameter name to property path of an entity](#mapping-http-parameter-name-to-property-path-of-an-entity)
   * [Static parts of queries](#static-parts-of-queries) -- adding static (not bound to HTTP params) predicates such as `deleted = false`
   * [Default value of queries](#default-value-of-queries) -- providing a fallback value when HTTP parameter is not present
   * [Annotated specification interfaces](#annotated-specification-interfaces) -- resolving specifications from annotated interfaces
      * [Interface inheritance tree](#interface-inheritance-tree)
   * [Handling different field types](#handling-different-field-types) -- handling situations when provided parameter is of different type than the field (e.g. `"abc"` sent against an integer field)
   * [Path Variable support](#path-variable-support) -- using uri fragments (resolvable with Spring's `@PathVariable` annotation) in specifications
   * [Type conversions for HTTP parameters](#type-conversions-for-http-parameters) -- information about supported type conversions (i.e. ability to convert HTTP parameters into Java types such as `LocalDateTime`, etc.) and the support of defining custom converters
   * [SpEL support](#spel-support) -- information about Spring Expression Language support
   * [Compatibility notes](#compatibility-notes) -- information about older versions compatible with previous Spring Boot and Java versions
   * [Download binary releases](#download-binary-releases) -- Maven artifact locations


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
public class MyConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver());
    }

    ...
}
```

The library converts HTTP parameters (strings) into most popular Java types such as dates, numbers, and enums. In case of a need for some additional conversion, please see how to configure specification argument resolver with If you need to use additional converters please see [custom converters section](#type-conversions-for-http-parameters).

Simple specifications
----------------------

Use `@Spec` annotation to automatically resolve a `Specification` argument of your controller method. `@Spec` has `path` property that should be used to specify property path of the attribute of an entity, e.g. `address.city`. By default it's also the name of the expected HTTP parameter, e.g. `GET http://myhost?address.city=Springfield`.

Use `spec` attribute of the annotation to specify one of the filtering strategies listed in the subsections below (e.g. Like, Equal, In)

For multi value filters like: `In.class`, `NotIn.class` there are two ways of passing multiple arguments. The first way is passing the same HTTP parameter multiple times as follows: 

    GET http://myhost/customers?gender=MALE&gender=FEMALE

The second way is the use `paramSeparator` attribute of `@Spec`, which determines the argument separator.
For example the following controller method:
```java
@RequestMapping(value = "/customers", params = "genderIn")
@ResponseBody
public Object findCustomersByGender(
	@Spec(path = "gender", params = "genderIn", paramSeparator = ",", spec = In.class) Specification<Customer> spec) {
	return customerRepo.findAll(spec);
}
```
will handle `GET http://myhost/customers?gender=MALE,FEMALE` in exactly the same way as `GET http://myhost/customers?gender=MALE&gender=FEMALE` (as one parameter with two values `["MALE","GENDER"]`). Without specifying `paramSeparator` param `gender=MALE,FEMALE` will be processed as single value: `["MALE,FEMALE"]`.

### Like ###

Filters using JPAQL `like` expression. It adds a wildcard `%` at the beginning and the end of the actual value, e.g. `(..) where firstName like %Homer%`.

Usage: `@Spec(path="firstName", spec=Like.class)`.

There are also other variants which apply the wildcard only on the beginning or the ending of the provided value: `StartingWith` and `EndingWith`.

The negated version is available: `NotLike` which executes queries such as `(..) where firstName not like %Homer%`

### LikeIgnoreCase  ###

Works as `Like`, but the query is also case-insensitive.

Usage: `@Spec(path="firstName", spec=LikeIgnoreCase.class)`.

There are also other variants which apply the wildcard only on the beginning or the ending of the provided value: `StartingWithIgnoreCase` and `EndingWithIgnoreCase`.

### Equal ###

Compares an attribute of an entity with the value of a HTTP parameter (exact match). E.g. `(..) where gender = FEMALE`.

Supports multiple data types: numbers, booleans, strings, dates, enums.

Usage: `@Spec(path="gender", spec=Equal.class)`.

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overriden with a configuration parameter (see `LessThan` below).

A negation for this specification is also available: `NotEqual`.

### EqualIgnoreCase ###

Works as `Equal`, but the query is also case-insensitive, could be used for fields of type: `String`, `Enum`.

A negation for this specification is also available: `NotEqualIgnoreCase`.

### In ###

Compares an attribute of an entity with multiple values of a HTTP parameter. E.g. `(..) where gender in (MALE, FEMALE)`.

Supports multiple data types: numbers, booleans, strings, dates, enums.

Usage: `@Spec(path="gender", spec=In.class)`.

HTTP request example:

    GET http://myhost/customers?gender=MALE&gender=FEMALE

or if `paramSeparator` is specified (eg. `@Spec(path="gender", paramSeparator=',', spec=In.class)`):

    GET http://myhost/customers?gender=MALE,FEMALE

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overridden with a configuration parameter (see `LessThan` below).

A negation for this specification is also available: `NotIn`.

### Null ###

Filters using `is null` or `is not null`, depending on the value of the parameter passed in. A value of `true` will filter for `is null`, and a value of `false` will filter for `is not null`.

The data type of the field specified in `path` can be anything, but the HTTP parameter must be a Boolean. You should use `params` attribute to make it clear that the parameter is filtering for null values.

Usage: `@Spec(path="activationDate", params="activationDateNull" spec=Null.class)`.

If you want the query to be static, i.e. not depend on any HTTP param, use `constVal` attribute of `Spec` annotation:

For example `@Spec(path="nickname", spec=Null.class, constVal="true")` will always add `nickname is null` to the query.

### NotNull ###

An inversion of `Null` described above, for better readability in some scenarios.

For example, consider a `deletedDate` field which is null when the entity is not deleted, and vice-versa. Then, you can introduce this mapping:

    @Spec(path="deletedDate", params="isDeleted", spec=NotNull.class)

to handle HTTP requests such as:

    GET http://myhost/customers?isDeleted=true
    GET http://myhost/customers?isDeleted=false

to return deleted (`deletedDate` not null) and not deleted (`deltedDate` null) respectively.

### GreaterThan, GreaterThanOrEqual, LessThan, LessThanOrEqual ###

Filters using a comparison operator (`>`, `>=`, `<` or `<=`). Supports multiple field types: strings, numbers, booleans, enums, dates. Field types must be Comparable (e.g, implement the Comparable interface); this is a JPA constraint.

Usage: `@Spec(path="creationDate", spec=LessThan.class)`.

For temporal values, the default date format is `yyyy-MM-dd`. You can override it by providing a config value to the annotation: `@Spec(path="creationDate", spec=LessThan.class, config="dd-MM-yyyy")`.

NOTE: comparisons are dependent on the underlying database.
 * Comparisons of floats and doubles (especially floats) may be incorrect due to precision loss.
 * Comparisons of booleans may be dependent on the underlying database representation.
 * Comparisons of enums will be of their ordinal or string representations, depending on what you specified to JPA, e.g., `@Enumerated(EnumType.STRING)`, `@Enumerated(EnumType.ORDINAL)` or the default (`@Enumerated(EnumType.ORDINAL)`)

### Between ###

Filters by checking if a comparable field of an entity is in the provided range. E.g. `(..) where creation date between :after and :before`.

It requires 2 HTTP parameters (for lower and upper bound). You should use `params` attribute of the `@Spec` annotation, i.e.: `@Spec(path="registrationDate", params={"registeredAfter","registeredBefore"}, spec=Between.class)`. The corresponding HTTP query would be: `GET http://myhost/customers?registeredAfter=2014-01-01&registeredBefore=2014-12-31`.

NOTE: comparisons are dependent on the actual type and the underlying database (see the explanation for `GreaterThan` above).

You can configure the date/datetime pattern as with `LessThan` described above.


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
Join
----

You can use `@Join` annotation to perform joins and then filter by attributes of joined entities. For example, let's assume the following entities:

```java
@Entity
public class Customer {

    // other fields omitted for brevity

    @OneToMany(mappedBy = customer)
    private Collection<Order> orders;

}

@Entity
public class Order {

    // other fields omitted for brevity

    @ManyToOne
    private Customer customer;

    private String itemName;
}

```

If you want to find all customers who ordered pizza, you can do the following:

```java
@RequestMapping("/customers")
public Object findByOrderedItem(
        @Join(path= "orders", alias = "o") // alias specified for joined path
        @Spec(path="o.itemName", params="orderedItem", spec=Like.class) // alias used in regular spec definition
        Specification<Customer> customersByOrderedItemSpec) {

    return customerRepo.findAll(customersByOrderedItemSpec);
}

```

The default join type is `INNER`. You can use `type` attribute of the annotation to specify different value.

Using `@Join` annotation makes the query distinct by default. While it is the best approach for most of the cases, you can override it by using `distinct` attribute of the annotation.

You can specify multiple different joins, for example:

```java
@RequestMapping("/customers")
public Object findByOrderedOrFavouriteItem(
        @Join(path = "orders", alias = "o")
        @Join(path = "favourites", alias = "f")
        @Or({
            @Spec(path="o.itemName", params="item", spec=Like.class),
            @Spec(path="f.itemName", params="item", spec=Like.class)}) Specification<Customer> customersByItem) {

    return customerRepo.findAll(customersByItem);
}
```

Multi-level joins are supported. To create multi-level join you should specify multiple joins in which join path contains alias of another join with higher priority. 

For example, let's assume the following entities:
```java
@Entity
class Customer {

    // other fields omitted for brevity

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders;

}

@Entity
class Order {

    // other fields omitted for brevity

    @ManyToOne
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<ItemTag> tags;

    ...
}

@Entity
public class ItemTag {

    // other fields omitted for brevity
	private String name;
    
    ...
}
``` 

If you want to find all customers who ordered item tagged as `#snacks`, you can do the following:
```java
@RequestMapping(value = "/findCustomersByOrderedItemTag")
@PostMapping
public Object findCustomersByOrderedItemTag(
		@Join(path = "orders", alias = "o")
		@Join(path = "o.tags", alias = "t")
		@Spec(path = "t.name", params = "tag", spec = Equal.class) Specification<Customer> spec) {
	return customerRepo.findAll(spec, Sort.by("id"));
}
```

__Annotations are processed sequentially, the order must be kept!__

Following spec is invalid:
```java
@RequestMapping(value = "/findCustomersByOrderedItemTag")
@PostMapping
public Object findCustomersByOrderedItemTag(
		@Join(path = "o.tags", alias = "t") // "o" alias will be not exist during processing this @Join
		@Join(path = "orders", alias = "o")
		@Spec(path = "t.name", params = "tag", spec = Equal.class) Specification<Customer> spec) {
	return customerRepo.findAll(spec, Sort.by("id"));
}
```

You can use join annotations with custom [annotated specification interfaces](#annotated-specification-interfaces).

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

As with `@Join`, the use of `@JoinFetch` makes the query distinct by default.
The default join type is `LEFT`. You can use `joinType` attribute of the annotation to specify different value. You can specify multiple different joins, for example:

```java
@RequestMapping("/customers")
public Object findByCityFetchOrdersAndAddresses(
        @JoinFetch(paths = "orders")
        @JoinFetch(paths = "addresses", joinType = JoinType.INNER)
        @Spec(path="address.city", params="town", spec=Like.class) Specification<Customer> customersByCitySpec) {

    return customerRepo.findAll(customersByCitySpec);
}
```

You can use join annotations with custom [annotated specification interfaces](#annotated-specification-interfaces).

Multi-level fetch join is supported. To create multi-level fetch join you should specify multiple fetch joins in which join path contains alias of another fetch join with higher priority.

For example, let's assume the following entities:
```java
@Entity
class Customer {

    // other fields omitted for brevity

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders;

}

@Entity
class Order {

    // other fields omitted for brevity

    @ManyToOne
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<ItemTag> tags;

    ...
}

@Entity
public class ItemTag {

    // other fields omitted for brevity
	private String name;
    
    ...
}
``` 

If you want to find all customers who ordered item with given tag name and fetch additional nested attributes (entities) to avoid `SELECT N+1 Problem` you can do the following:
```java
@RequestMapping(value = "/findCustomersWhoOrderedItemWithGivenTag")
@PostMapping
public Object findCustomers(
	    @JoinFetch(paths = "orders", alias = "o")
	    @JoinFetch(paths = "o.tags", alias = "t")
	    @Spec(path = "t.name", params = "tagName", spec = Equal.class) Specification<Customer> spec) {
	return customerRepo.findAll(spec);
}
```

The same as in case of multi-level joins, annotations are processed sequentially, the order must be kept!

  Please remember that:
  * Join fetch path can use only aliases of another fetch joins. 
  * Join path can use only aliases of another joins. 

  Following combinations are forbidden:
  
   * join path which uses join fetch alias  
  
        ```java
         @RequestMapping(value = "/findCustomersByOrderedItemTag", params = { "tagName" })
         @PostMapping
         public Object findCustomers(
         	    @JoinFetch(paths = "orders", alias = "o")
        	    // Wrong, 'o' defined in join fetch tried to be used in a regular join
         	    @Join(path = "o.tags", alias = "t")
        	    @Spec(path = "t.name", params = "tagName", spec = Equal.class) Specification<Customer> spec) {
         	return repository.findAll(spec);
         }
        ```
   * join fetch path which uses join alias
    
       ```java
        @RequestMapping(value = "/findCustomersByOrderedItemTag", params = { "tagName" })
        @PostMapping
        public Object findCustomers(
        	    @Join(path = "orders", alias = "o")
        	    // Wrong, 'o' defined in a join tried to be used in join fetch
        	    @JoinFetch(paths = "o.tags", alias = "t")
        	    @Spec(path = "t.name", params = "tagName", spec = Equal.class) Specification<Customer> spec) {
        	return repository.findAll(spec);
        }
       ```
     
      If the same alias is defined both for `@Join` and `@JoinFetch`, the join alias will be used during specification resolving.
      
       ```java
        @RequestMapping(value = "/findCustomersByOrderedItemTag", params = { "tagName" })
        @PostMapping
        public Object findCustomers(
        	    @JoinFetch(paths = "orders", alias = "o")
        	    @JoinFetch(paths = "o.tags", alias = "t")
        	    @Join(path = "orders", alias = "o")
        	    @Join(path = "o.tags", alias = "t")
        	    //'t' refers to third join - @Join(path = "o.tags", alias = "t")
        	    @Spec(path = "t.name", params = "tagName", spec = Equal.class) Specification<Customer> spec) {
         
             return repository.findAll(spec);
        }
       ```
     Using join and join fetch on the same path should be avoided, otherwise the same table will be joined twice (`orders` and `tags` in above example).
     The above example is an anti-pattern and should never be followed in the production code.

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

will always produce the following: `where deleted = false`. It is often convenient to combine such a static part with dynamic ones using `@And` or `@Or` described below.

Support for [SpEL](https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/core.html#expressions) expression and [property placeholders]((https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html)) in `constVal` could be enabled in following way:
* Configure `SpecificationArgumentResolver` by passing [AbstractApplicationContext](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/AbstractApplicationContext.html) in constructor
* Set attribute `valueInSpEL` value to `true`
  
Configuration example:
   ```java
   @Autowired
   AbstractApplicationContext applicationContext;
    
   @Override
   public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
   	argumentResolvers.add(new SpecificationArgumentResolver(applicationContext));
   }
   ```
  

Usage example #1:
   ```java
   @RequestMapping(value = "/customers")
   @ResponseBody
   public Object findCustomersBornInTheFuture(
           @Spec(path = "birthDate", constVal = "#{T(java.time.LocalDate).now()}", valueInSpEL = true, spec = GreaterThanOrEqual.class) Specification<Customer> spec) {
   	
   	return customerRepo.findAll(spec);
   }
   ```

Usage example #2:
   ```java
   @RequestMapping(value = "/customers")
   @ResponseBody
   public Object findCustomersWithTheLastNameSimpson(
           @Spec(path = "lastName", constVal = "${search.default-params.lastName}", valueInSpEL = true, spec = Equal.class) Specification<Customer> spec) {
   	
   	return customerRepo.findAll(spec);
   }
   ```

   application.properties
   ```properties
   search.default-params.lastName=Simpson
   ```
   
Defined in `constVal` SpEL expression should be able to be evaluated to `java.lang.String`. 


Default value of queries
------------------------

When the request parameter is not provided or has an empty value, you can use `defaultVal` atatribute of `@Spec` to provide a value to fallback to. For example this controller method:

```java
@RequestMapping("/users")
public Object findByRole(
                  @Spec(path="role", spec=Equal.class, defaultVal="USER") Specification<User> spec) {

    return userRepo.findAll(spec);
}
```

Would handle request such as `GET /users` with the following query: `select u from Users u where u.role = 'USER'`.


Supplying `constVal` implicitly sets `defaultVal` to empty.

Support for [SpEL](https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/core.html#expressions) expression and [property placeholders]((https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html)) in `defaultVal` could be enabled in the same way as for [constVal](#static-parts-of-queries)

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

### Interface inheritance tree

Specifications are resolved from all parent interfaces and combined with `and`. As an example, let's consider the following interfaces:

```java
@Spec(path = "deleted", constVal = "false", spec = Equal.class)
public interface NotDeletedSpec extends Specification<Customer> {}

@Spec(path = "firstName", spec = Equal.class)
public interface FirstNameSpec extends NotDeletedSpec {}
```

`FirstNameSpec` extends `NotDeletedSpec`, so their specifications will be combined with `and`, i.e. a controller method like this:

```java
@RequestMapping("/customers")
@ResponseBody
public Object findNotDeletedCustomersByFirstName(FirstNameSpec spec) {
    
  return repository.findAll(spec);
}
```

will accept HTTP requests such as `GET /customers?firstName=Homer` and execute JPA queries such as `where firstName = 'Homer' and deleted = false`.

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


Path variable support
---------------------

Although in pure RESTful API this feature should not be needed, it sometimes might be useful to use values from path variables. Path variables are uri fragments resolvable with Spring's `@PathVariable` annotation. You can refer to them by using `pathVars` property of `@Spec` (instead of `params` property). For example:

  ```java
  @RequestMapping("/customers/{customerLastName}")
  @ResponseBody
  public Object findNotDeletedCustomersByFirstName(
                       @Spec(path = "lastName", pathVars = "customerLastName", spec=Equal.class) Specification<Customer> spec) {

    return repository.findAll(spec);
  }
  ```

This will handle request `GET /customers/Simpson` as `select c from Customers c where c.lastName = 'Simpson'`.

Basic regular expressions are supported for path variable matching. All patterns supported by [Spring AntPathMatcher](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html) are supported. For example:

```java
@RequestMapping(value = "/pathVar/customers/{customerId:[0-9]+}")
@ResponseBody
public Object findById(
  @Spec(path = "id", pathVars = "customerId", spec = Equal.class) Specification<Customer> spec) {

  return customerRepo.findAll(spec);
}
```

Request Header Support
---------------------

This is not a best practice for RESTful services but sometimes you may want to filter with request header's value. You
can resolve request headers with Spring's `@RequestHeader` annotation. You can refer to them by using `headers` property
of `@Spec` (instead of `params` property). For example:

  ```java
@RequestMapping(value = "/customers/reqHeaders")
@ResponseBody
public Object findCustomersByGenderAndNickName(
    @RequestHeader(value = "customerGender", required = false) String gender,
    @RequestHeader(value = "customerNickName", required = false) String nickName,
    @And({
            @Spec(path = "nickName", headers = "customerNickName", spec = Equal.class),
            @Spec(path = "gender", headers = "customerGender", spec = Equal.class)
    }) Specification<Customer> spec){
    return customerRepo.findAll(spec);
}
  ```

This will handle request `GET /customers/reqHeaders` as `select c from Customers c where c.gender = :gender AND c.nickName = :nickName`.


Type conversions for HTTP parameters
-------------------

Specification argument resolvers uses conversion mechanism to convert request string params to types of fields for which specifications have been defined.

Let's consider the following code:
  ```java
	@Controller
	public class SampleController {
		
		@Autowired
		CustomerRepository customerRepository;
		
		@RequestMapping("/find")
		public List<Customer> findByRegistrationDate(
				@And({
					@Spec(path = "name", params = "name", spec = Equal.class),
					@Spec(path = "registrationDate", params = "registrationDate", spec = GreaterThanOrEqual.class)
				}) Specification<Customer> spec) {
			return customerRepository.findAll(spec);
		}
	}
	
	@Entity
	public class Customer {
		String name;
		Date registrationDate;
	}
  ``` 
When the following request will be sent to the endpoint presented above
  ```
  GET /find?name=John&registrationDate=2020-06-19
  ```

a `Specification<Customer>` based on fields `name` and `registrationDate` will be built.

  * The type of the `name` field is a `String` type, received parameter value is always a `String` type so there is no need of conversion.  
  * The type of the `registartionDate` field is `java.util.Date` type, so the `String` will be converted into `Date` using one of available converter.

##### Supported conversions  
Specification Argument Resolver contains converters for most common java types. 

List of supported conversions:

  * `String -> Enum`
  * `String -> boolean`
  * `String -> Boolean`
  * `String -> integer`
  * `String -> Integer`
  * `String -> long`
  * `String -> Long`
  * `String -> float`
  * `String -> Float`
  * `String -> double`
  * `String -> Double`
  * `String -> BigDecimal`
  * `String -> Date` (default format: `yyyy-MM-dd`)
  * `String -> Calendar` (default format: `yyyy-MM-dd`)
  * `String -> LocalDate` (default format: `yyyy-MM-dd`)
  * `String -> LocalDateTime` (default format: `yyyy-MM-dd'T'HH:mm:ss`)
  * `String -> OffsetDateTime` (default format: `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`)
  * `String -> Instant` (default format: `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`)
  * `String -> Timestamp` (default format: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`)
  * `String -> UUID` 

To use a custom format for temporal types, add `config="custom-format-value"` to `@Spec` params. 
For example:
```
 @Spec(path="creationDate", spec=LessThan.class, config="dd-MM-yyyy")
```


In case of missing converter, [fallback mechanism](#custom-converters) will be used if one has been configured otherwise `ClassCastException` will be thrown.

##### Custom converters
The converter includes a fallback mechanism based on [Spring](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html) `ConversionService`, which is invoked when required conversion is not supported by any default converter. If the `ConversionService` supports required conversion it will be performed, otherwise a `ClassCastException` will be thrown. 

To add specification argument resolver support for custom conversion:
1) Add required converter to [ConversionService](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html)
2) Configure fallback mechanism by passing [ConversionService](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html) to SpecificationArgumentResolver constructor.


For example:

```java
@Configuration
@EnableJpaRepositories
public class MyConfig implements WebMvcConfigurer {
 
    @Autowired
    ConversionService conversionService;
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver(conversionService));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToAddressConverter());
    }
 
    public static class StringToAddressConverter implements Converter<String, Address> {
        @Override
        public Address convert(String rawAddress) {
            Address address = new Address();
            address.setStreet(rawAddress);
            return address;
        }
    }

    ...
}
```

SpEL support
------------

Support for [SpEL](https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/core.html#expressions) expression and [property placeholders]((https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html)) can be enabled in following way:
* Configure `SpecificationArgumentResolver` by passing [AbstractApplicationContext](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/AbstractApplicationContext.html) in constructor
* Set attribute `valueInSpEL` value to `true`

Configuration example:
   ```java
   @Autowired
   AbstractApplicationContext applicationContext;

   @Override
   public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver(applicationContext));
   }
   ```

SpEL expressions can be applied to `@Spec` `constVal`, `defaultVal` and `params`. The first two are described in more detail in corresponding sections above. SpEL support for `params` can be enabled via `@Spec.paramsInSpEL`. It may be useful in rare cases when you want to differentiate HTTP parameter name based on the application configuration or other contextual attributes.

Cache support
------------

Specification argument resolver supports [spring cache](https://docs.spring.io/spring-boot/docs/2.6.x/reference/html/io.html#io.caching). Equals and HashCode contract is satisfied for generated specifications.

Compatibility notes
-------------------

This project has been maintained since 2014. A lot has changed in Java and Spring during that period and the most recent versions might not be compatible with older JDK and/or Spring. In the table below you can find the summary of version compatibility:

| specification-arg-resolver version | JDK requirements | Spring requirements                                                                     |
|------------------------------------|------------------|-----------------------------------------------------------------------------------------|
| `v2.0.0` (or newer)                | `1.8` or higher  | Compiled and tested against Spring Boot `2.6.13`                                        |
| `v1.1.1` (or older)                | `1.7` or higher  | Compiled and tested against Spring Boot `1.x`; confirmed to work with Spring boot `2.x` |

As far as the features supported in each version, please check the [CHANGELOG.md](https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/CHANGELOG.md)


Download binary releases
------------------------

Specification argument resolver is available in the Maven Central:

```xml
<dependency>
    <groupId>net.kaczmarzyk</groupId>
    <artifactId>specification-arg-resolver</artifactId>
    <version>2.9.0</version>
</dependency>
```

If a new version is not yet available in the central repository (or you need a SNAPSHOT version), you can grab it from my private repo:

```xml
<repository>
    <id>kaczmarzyk.net</id>
    <url>http://repo.kaczmarzyk.net</url>
</repository>
```
