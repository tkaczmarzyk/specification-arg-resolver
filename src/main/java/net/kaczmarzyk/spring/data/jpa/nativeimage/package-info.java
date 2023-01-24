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
package net.kaczmarzyk.spring.data.jpa.nativeimage;

/**
 *
 * This package provides support for using specification-arg-resolver in Spring Native images for GraalVM.
 * Due to mechanisms (reflection) used by this library, some addional configuration steps are needed in order to enable such support.
 *
 * The specification-argument-resolver uses a two mechanisms which are not supported by GraalVM native image by default:
 *
 *  1) Reflection:
 *    * {@link net.kaczmarzyk.spring.data.jpa.web.SimpleSpecificationResolver} uses reflection during creation a specification with defined type.
 *    * "Native Image has partial support for reflection and needs to know ahead-of-time the reflectively accessed program elements." (https://www.graalvm.org/22.0/reference-manual/native-image/Reflection/)
 *    * To enable support for resolving specifications the library user:
 *      a) has to prepare a manual config for all classes (it's not convenient for the user however it is technically possible)
 *      b) (preferred) has to import runtime hints registered by {@link net.kaczmarzyk.spring.data.jpa.nativeimage.SpecificationArgumentResolverHintRegistrar}
 *
 *  2) Dynamic proxy:
 *    * For the specification defined in interfaces {@link net.kaczmarzyk.spring.data.jpa.web.EnhancerUtil} generates dynamic proxy in runtime.
 *      It's not supported by GraalVM native-image, from documentation:
 *        "Native Image does not provide machinery for generating and interpreting bytecodes at run time.
 *         Therefore all dynamic proxy classes need to be generated at native image build time."
 *        (https://www.graalvm.org/22.0/reference-manual/native-image/DynamicProxy/)
 *    * The GraalVM native-image has a mechanism of automated dynamic proxy detection, however this mechanism does not cover our case.
 *    * To enable support for specifications defined in interfaces the library user:
 *       a) has to prepare a manual config for such classes (see graalvm documentation for details)
 *       b) (preffered) has to import runtime hints registered by {@link net.kaczmarzyk.spring.data.jpa.nativeimage.SpecificationArgumentResolverProxyHintRegistrar} - it uses classgraph library to find on classpath interfaces with sar annotations and register for them dynamic proxy hints.
 *
 *  The information about the fundamentals of GraalVM native image could be found in the GraalVM documentation:
 *  * https://www.graalvm.org/22.0/reference-manual/native-image/
 *
 *  The information about the spring-boot native image support ({@link org.springframework.aot.hint.RuntimeHintsRegistrar} could be found in the spring boot documentation:
 *  * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image
 *
 */
