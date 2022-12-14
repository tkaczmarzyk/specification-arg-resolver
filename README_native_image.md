Spring native image / GraalVM native image support
------------

### General comment
The information about the spring GraalVM native image support could be found in the [spring-boot-documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image).

### Why specification-argument-resolver does not support native-image by default?
The specification-argument-resolver uses two mechanisms that are not supported by graalvm native image by default:
* Reflection - it's used to construct specification instances (`SimpleSpecificationResolver.java`).
* Dynamic proxy class - it's used for proxy creation for specifications defined in interfaces.
    * For example, consider following specification and endpoint:
       ```java
       @Or({
           @Spec(path="firstName", params="name", spec=Like.class), 
           @Spec(path="lastName", params="name", spec=Like.class)
       })
       public interface FullNameSpec extends Specification<Customer> {}
      
       @RequestMapping("/customers")
       @ResponseBody
       public Object findByFullName(FullNameSpec spec) {
           return repository.findAll(spec);
       }
       ```
      For the above specification, the `Disjunction` specification with two inner specifications of type `Like` is created.
      However, the endpoint signature requires a argument of type `FullNameSpec` not the `Disjunction`.
      So when the specification-argument-resolver detect such argument incompatibility (`Disjunction` vs `FullNameSpec`) it generates (`EnhancerUtil.java`)
      the proxy for `Disjunction` of type `FullNameSpec`.

#### How to enable native-image support?
* To enable of basic-support of native-image (assuming that specifications are not defined in interfaces),
  the specification argument resolver hints registrar should be imported to your app - `SpecificationArgumentResolverHintRegistrar.java`.
  It registers a constructors of specification classes defined in package `net.kaczmarzyk.spring.data.jpa.domain` for reflection.
  Example config:
  ```
  @Configuration
  @ImportRuntimeHints(SpecificationArgumentResolverHintRegistrar.class)
  public class AppConfig{}
  ```
* To enable full support you should enable support for dynamic proxy for your interfaces with specification definitions.
    * You should manually register spec-interfaces for dynamic proxy
      or
    * You should extend `SpecificationArgumentResolverProxyHintRegistrar.java` and set the packages with interfaces containing specification definitions, and import hints from this registrar.
      This registrar scans the classpath looking for a interfaces with specification-argument-resolver annotations, and register the found interfaces for dynamic proxy.
      Example:
  ```
  class ProjectSpecificationArgumentResolverProxyHintRegistrar extends SpecificationArgumentResolverProxyHintRegistrar {
      protected MyProjectSpecificationArgumentResolverProxyHintRegistrar() {
          super(
                  "net.kaczmarzyk" // the name of package containing the interfaces with specification definitions
          );
      }
  }
  ```
  and then in config:
  ```
  @Configuration
  @ImportRuntimeHints(SpecificationArgumentResolverHintRegistrar.class) //suport for reflection
  @ImportRuntimeHints(MyProjectSpecificationArgumentResolverProxyHintRegistrar.class) //suport for dynamic proxy
  public class AppConfig{}
  ```

  The `SpecificationArgumentResolverProxyHintRegistrar.java` requires dependency:
  ```
  <dependency>
      <groupId>io.github.classgraph</groupId>
      <artifactId>classgraph</artifactId>
      <version>4.8.X</version>
  </dependency>
