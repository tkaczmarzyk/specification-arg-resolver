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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.kaczmarzyk.spring.data.jpa.domain.PathSpecification;
import org.junit.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Radlica
 */
public class SpecificationArgumentResolverHintRegistrarTest {

    @Test
    public void registersReflectionHintsForEachSpecificationType() {
        //given
        RuntimeHints runtimeHints = new RuntimeHints();
        ClassLoader classLoader = this.getClass().getClassLoader();

        List<ClassInfo> specificationClasses = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("net.kaczmarzyk.spring.data.jpa.domain")
                .scan()
                .getSubclasses(PathSpecification.class)
                .stream()
                .filter(clazz -> !clazz.isAbstract())
                .toList();

        //additional check that each specification type has been discovered
        assertThat(specificationClasses)
                .hasSize(36);

        SpecificationArgumentResolverHintRegistrar hintRegistrar = new SpecificationArgumentResolverHintRegistrar();

        //when
        hintRegistrar.registerHints(runtimeHints, classLoader);

        //then
        assertThat(runtimeHints.reflection().typeHints())
                        .hasSize(36);

        specificationClasses.forEach(domainClass -> {
            TypeHint classReflectionHints = runtimeHints.reflection().getTypeHint(domainClass.loadClass());

            int numberOfConstructorsRegisteredForReflection =
                    nonNull(classReflectionHints) ? (int) classReflectionHints.constructors().count() : 0;

            int numberOfClassConstructors = domainClass.getDeclaredConstructorInfo().size();

            assertThat(numberOfConstructorsRegisteredForReflection)
                    .withFailMessage(
                            "[%s] Expected number of constructors registered for reflections: %s, actual: %s",
                            domainClass.getName(),
                            numberOfClassConstructors,
                            numberOfConstructorsRegisteredForReflection
                    )
                    .isGreaterThan(0)
                    .isEqualTo(numberOfClassConstructors);
        });
    }

}
