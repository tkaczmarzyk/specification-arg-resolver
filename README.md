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

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overriden with a configuration parameter (see `DateBefore` below).

### In ###

Compares an attribute of an entity with multiple values of a HTTP parameter. E.g. `(..) where gender in (MALE, FEMALE)`.

HTTP request example:

    GET http://myhost/customers?gender=MALE&gender=FEMALE

Supports multiple data types: numbers, booleans, strings, dates, enums.

Usage: `@Spec(path="gender", spec=In.class)`.

The default date format used for temporal fields is `yyyy-MM-dd`. It can be overriden with a configuration parameter (see `DateBefore` below).

### IsNull ###

Does not use any HTTP-parameters. Represents static `where` clause: `path is null`.

Usage: `@Spec(path="activationDate", spec=IsNull.class)`.

### DateBefore ###

Filters by checking if a temporal field of an entity has a value before the given one. E.g. `(..) where creationDate < :date`.

Usage: `@Spec(path="creationDate", spec=DateBefore.class)`.

The default date format is `yyyy-MM-dd`. You can override it by providing a config value to the annotation: `@Spec(path="creationDate", spec=DateBefore.class, config="dd-MM-yyyy")`.

### DateAfter ###

Filters by checking if a temporal field of an entity has a value after the given one. E.g. `(..) where creationDate > :date`.

Usage: `@Spec(path="creationDate", spec=DateAfter.class)`.

You can configure the date pattern as with `DateBefore` described above.

### DateBetween ###

Filters by checking if a temporal field of an entity is in the provided date range. E.g. `(..) where creation date between :after and :before`.

It requires 2 HTTP parameters (for lower and upper bound). You should use `params` attribute of the `@Spec` annotation, i.e.: `@Spec(path="registrationDate", params={"registeredAfter","registeredBefore"}, spec=DateBetween.class)`. The corresponding HTTP query would be: `GET http://myhost/customers?registeredAfter=2014-01-01&registeredBefore=2014-12-31`.

You can configure the date pattern as with `DateBefore` described above.

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

Download binary releases
------------------------

Specification argument resolver is available in the Maven Central:

```xml
<dependency>
    <groupId>net.kaczmarzyk</groupId>
    <artifactId>specification-arg-resolver</artifactId>
    <version>0.6.0</version>
</dependency>
```

If a new version is not yet available in the central repository, you can grab it from my private repo:

```xml
<repository>
    <id>kaczmarzyk.net</id>
    <url>http://repo.kaczmarzyk.net</url>
</repository>
```
