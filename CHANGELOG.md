v2.6.2
======
* fixed pagination support for multi-level joins

v2.6.1
======
* fixed bug which caused invalid query to be created when multiple `@JoinFetch` annotations referenced the same alias

v2.6.0
======
* Added support for multi-level joins. 

  It's now possible to define multi-level join where each join can use aliases defined by previous joins (see [README.md](https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README.md) for the details).
  
  For example:
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
  
  Multi-level join fetch could be defined similarly to multi-level join.
 
  For example:
  ```java
  @RequestMapping(value = "/findCustomers")
  @PostMapping
  public Object findAllCustomers(
  		@JoinFetch(paths = "orders", alias = "o")
  		@JoinFetch(paths = "o.tags") Specification<Customer> spec) {
  	return customerRepo.findAll(spec).stream()
  			.map(this::mapToCustomerDto)
  			.collect(toList());

* Added support for [SpEL](https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/core.html#expressions) and [property placeholders](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html) in `@Spec` attributes: `constVal`, `defaultVal`.
 
  To enable SpEL support:
  * Configure `SpecificationArgumentResolver` by passing `AbstractApplicationContext` in constructor
  * Set `Spec` attribute `valueInSpEL` value to `true` 
  
   Configuration example:
   ```java
  	@Autowired
  	AbstractApplicationContext applicationContext;
  	
  	@Override
  	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
  		argumentResolvers.add(new SpecificationArgumentResolver(applicationContext));
  	}
  ```

  Usage example of default value with property placeholder:

  ```java
  @RequestMapping(value = "/customers")
  @ResponseBody
  public Object findByLastName(
          @Spec(path = "id", params="lastName", defaultVal='${search.default-params.lastName}', valueInSpEL = true, spec = Equal.class) Specification<Customer> spec) {
  	
  	return customerRepo.findAll(spec);
  }
  ```

  application.properties
  ```properties
  search.default-params.lastName=Simpson
  ```

  Usage example of default value in [SpEL](https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/core.html#expressions):

  ```java
  @RequestMapping(value = "/customers")
  @ResponseBody
  public Object findCustomersWhoCameFromTheFuture(
          @Spec(path = "id", params="birthDate", defaultVal='#{T(java.time.LocalDate).now()}', valueInSpEL = true, spec = GreaterThanOrEqual.class) Specification<Customer> spec) {
  	
  	return customerRepo.findAll(spec);
  }
  ```

v2.5.0
======
* Added support for repeatable `@Join` and `@JoinFetch` annotations. `@Joins` annotation is now deprecated and it's going to be removed in the future.

    To specifying multiple different joins, repeated `@Join` annotation should be used: 
    ```java
    @RequestMapping(value = "/findBy", params = {""})
    public void findByBadgeTypeAndOrderItemName(
    		@Join(path = "orders", alias = "o", type = JoinType.LEFT)
    		@Join(path = "badges", alias = "b", type = JoinType.LEFT)
    		@Or({
    				@Spec(path = "o.itemName", params = "order", spec = Like.class),
    				@Spec(path = "b.badgeType", params = "badge", spec = Equal.class)
    		}) Specification<Customer> spec) {
    	return customerRepository.findAll(spec);
    }
    ```
    instead of using annotation container `@Joins`.

* Added support for enum in specs: `EqualIgnoreCase.class`, `NotEqualIgnoreCase.class`

v2.4.2
======
* Fixed `NullPointerException` for requests with missing params to an endpoint with specs which uses param separator. In previous versions `NullPointerException` had been thrown for requests with missing parameters. Now spec with `paramSeparator` attribute is skipped for request with missing params.

v2.4.1
======
* Added `distinct` (default: `true`) attribute to `JoinFetch` annotation. Attribute determines that query should be distinct or not.

v2.4.0
======
* Added conversion support for `UUID`, `OffsetDatetime`, `Instant`

* Added a fallback mechanism to `Converter` which uses converters registered in ConversionService. 
The `Converter` in case of missing converter for a given type tries to find a required converter in [Spring](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html) `ConversionService` , if `ConversionService` does not support required conversion `IllegalArgumentException` will be thrown. If the required converter is not present in `Converter` and `ConversionService` it could be defined and used as follows:

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

v2.3.0
======

* Added support for path variables with regexp. All patterns supported by [spring AntPathMatcher](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html) are supported.
  
  For example:
  ```java
  @RequestMapping(value = "/pathVar/customers/{customerId:[0-9]+}")
  @ResponseBody
  public Object findById(
          @Spec(path = "id", pathVars = "customerId", spec = Equal.class) Specification<Customer> spec) {
  	
  	return customerRepo.findAll(spec);
  }
  ```

v2.2.2
======

* Fixed support for custom interfaces with complex inheritance tree. In previous versions, annotations: @Join, @JoinFetch, @Joins were supported only for the lowest interface in the inheritance tree. 

    Following example didn't work before fix:
     ```java
      @Join(path= "orders", alias = "o")
      @Spec(path="o.id", params="orderId", spec=Equal.class)
      public interface CommonFilter<T> extends Specification<T> { }
      
      public interface CustomerFilter extends CommonFilter<Customer> { }
     ```

v2.2.1
======

* `WebRequestQueryContext` has been improved to use an actual root query instance during path evaluation, rather than the one being cached before. **This should fix problems with count queries**
* Made `@JoinFetch` query distinct by default to keep the behavior in line with `@Join`.

v2.2.0
======

* Added support for passing multiple values as a single HTTP parameter. The new `paramSeparator` attribute of `@Spec` can be used to define the separator (e.g. comma). For example the following controller method:

  ```java
  @RequestMapping(value = "/customers", params = "genderIn")
  @ResponseBody
  public Object findCustomersByGender(
  	@Spec(path = "gender",
              params = "genderIn",
              paramSeparator = ",",
              spec = In.class) Specification<Customer> spec) {

  	return customerRepo.findAll(spec);
  }
  ```
  will handle `GET http://myhost/customers?gender=MALE,FEMALE` in exactly the same way as `GET http://myhost/customers?gender=MALE&gender=FEMALE`

v2.1.1
======

* fixed path variable resolving in environments where `HttpServletRequest.getPathInfo()` returns `null`

v2.1.0
======

* added possibility to define a default value for filtering, as a fallback when HTTP param is not present. For example this controller method: 
  ```java
  @RequestMapping("/users")
  public Object findByRole(
                    @Spec(path="role", spec=Equal.class, defaultVal="USER") Specification<User> spec) {
  
      return userRepo.findAll(spec);
  }
  ```
  
  Would handle request such as `GET /users` with the following query: `select u from Users u where u.role = 'USER'`.
* added new specifications: `StartingWith`, `EndingWith` and their case-insensitive counterparts
* added new specification negations: `NotIn`, `NotLike`

v2.0.0
======

* requires Java 8 + intended for Spring Boot 2.x
* fixed bug with repeated joins
* optimized joining: joins will not be performed if no filtering is applied on the join path
* fixed `OnTypeMismatch` behaviour for primitive `int` and `long` types
* under the hood improvements for better performance
* support for Java 8's `LocalDate` and `LocalDateTime`
* introduced `Between` specification which supports all `Comparable` types. Therefore `DateBetween` is now deprecated
* path variables are now supported! You can use new `pathVars` property of `@Spec` as follows:

  ```java
  @RequestMapping("/customers/{customerLastName}")
  @ResponseBody
  public Object findNotDeletedCustomersByFirstName(
                       @Spec(path = "lastName", pathVars = "customerLastName", spec=Equal.class) Specification<Customer> spec) {

    return repository.findAll(spec);
  }
  ```

  This will handle request `GET /customers/Simpson` as `select c from Customers c where c.lastName = 'Simpson'`.
* better conversion support for `float`, `double` and `BigDecimal`


v1.1.1
======

* bug fixes

v1.1.0
======

* added `NotEqual` and `NotEqualIgnoreCase` specs
* resolving annotations from parent interfaces, for example, consider the following interfaces:

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
 

v1.0.0
======

* join support! It is now possible to filter by attributes of joined entities. For example:

  ```java
  @RequestMapping("/customers")
  @ResponseBody
  public Object findByOrders(
          @Join(path = "orders", alias = "o")
          @Spec(paths = "o.itemName", params = "orderItem", spec=Like.class)
          Specification<Customer> spec) {

      return repository.findAll(spec);
  }  
  ```
  
  Of course you can use `@Join` on annotated custom specification interfaces:

  ```java
  @Join(path = "orders", alias = "o")
  @Spec(paths = "o.itemName", params = "orderItem", spec=Like.class)
  public interface CustomerByOrdersSpec implements Specification<Customer> {
  }

  // ...

  @RequestMapping("/customers")
  @ResponseBody
  public Object findByOrders(
          CustomerByOrdersSpec spec) {

      return repository.findAll(spec);
  }

  ```

* `@Joins` annotation has been changed to take instances of `@Join` as `value` parameter (was `@JoinFetch`). `@JoinFetch` might be passed to `join` param of `@Joins` 

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
