/**
 * Copyright 2014-2022 the original author or authors.
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
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * This RuntimeHintsRegistrar implementation scans classpath and registers jdk proxy for classes annotated with specification-argument-resolver annotations.
 *
 * @author Jakub Radlica
 */
public abstract class SpecificationArgumentResolverProxyHintRegistrar implements RuntimeHintsRegistrar {

    public static final String SPECIFICATIONS_PACKAGE_NAME = "net.kaczmarzyk.spring.data.jpa";
    private final String[] acceptPackages;

    /**
     * @param packagesWithInterfacesContainingSpecificationDefinitions - the packages which should be scanned
     */
    protected SpecificationArgumentResolverProxyHintRegistrar(String... packagesWithInterfacesContainingSpecificationDefinitions) {
        this.acceptPackages = packagesWithInterfacesContainingSpecificationDefinitions;
    }

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(acceptPackages)
                .scan()
                .getAllInterfaces()
                .stream()
                .filter(this::hasSarAnnotations)
                .forEach(classWithSarAnnotation -> {
                    hints.proxies().registerJdkProxy(classWithSarAnnotation.loadClass());
                });
    }

    private boolean hasSarAnnotations(ClassInfo potentialSpecificationInterface) {
        return potentialSpecificationInterface.getAnnotationInfo().stream()
                .anyMatch(annotation -> annotation.getClassInfo().getName().startsWith(SPECIFICATIONS_PACKAGE_NAME));
    }

}
