/**
 * Copyright 2014-2025 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.JdkProxyHint;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Radlica
 */
public class SpecificationArgumentResolverUserInterfacesHintRegistrarTest {

    @Conjunction(value = @Or({
            @Spec(params = "lastName", path = "lastName", spec = Equal.class),
            @Spec(params = "firstName", path = "firstName", spec = Equal.class)
    }), and = {
            @Spec(params = "gender", path = "gender", spec = Equal.class)
    })
    public interface InterfaceWithSpecificationDefinition {}

    public interface InterfaceWithoutSpecificationDefinition {}

    @Test
    public void registersDynamicProxyForInterfacesWithSpecificationDefinitions() {
        //given
        SpecificationArgumentResolverProxyHintRegistrar specificationArgumentResolverUserInterfacesHintRegistrar =
                new MyProjectSpecificationArgumentResolverProxyHintRegistrar();

        RuntimeHints runtimeHints = new RuntimeHints();
        ClassLoader classLoader = this.getClass().getClassLoader();

        //when
        specificationArgumentResolverUserInterfacesHintRegistrar.registerHints(
                runtimeHints,
                classLoader
        );

        //then
        List<JdkProxyHint> registeredJdkProxyHints = runtimeHints.proxies().jdkProxyHints().toList();

        assertThat(registeredJdkProxyHints)
                .hasSize(1);

        TypeReference proxiedInterface = registeredJdkProxyHints.get(0).getProxiedInterfaces().get(0);

        assertThat(proxiedInterface.getCanonicalName())
                .isEqualTo(
                        "net.kaczmarzyk.spring.data.jpa.nativeimage.SpecificationArgumentResolverUserInterfacesHintRegistrarTest.InterfaceWithSpecificationDefinition"
                );

    }


    class MyProjectSpecificationArgumentResolverProxyHintRegistrar extends SpecificationArgumentResolverProxyHintRegistrar {
        protected MyProjectSpecificationArgumentResolverProxyHintRegistrar() {
            super(
                    "net.kaczmarzyk.spring.data.jpa.nativeimage"
            );
        }
    }

}
