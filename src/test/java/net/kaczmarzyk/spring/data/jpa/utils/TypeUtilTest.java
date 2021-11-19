/**
 * Copyright 2014-2020 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;

import org.junit.jupiter.api.Test;

public class TypeUtilTest {

	public static class Clazz {}
	
	public static interface StandaloneIface {}
	
	public static interface StandaloneIface2 {}
	
	public static interface ChildIface extends StandaloneIface, StandaloneIface2 {}
	
	public static interface ChildIface2 extends StandaloneIface, StandaloneIface2 {}
	
	public static interface GrandChildIface extends ChildIface, ChildIface2 {}
	
	public static interface GrandGrandChildIface extends GrandChildIface {}
	
	@Test
	public void throwsExceptionIfProvidedTypeIsNotInterface() {
		assertThrows(IllegalArgumentException.class, () -> TypeUtil.interfaceTree(Clazz.class));
	}
	
	@Test
	public void returnsProvidedInterfaceItselfIfItDoesNotHaveAnyAncestors() {
		Collection<Class<?>> ifaces = TypeUtil.interfaceTree(StandaloneIface.class);
		
		assertThat(ifaces).containsOnly(StandaloneIface.class);
	}
	
	@Test
	public void resolvesAllParentIfaces() {
		Collection<Class<?>> ifaces = TypeUtil.interfaceTree(ChildIface.class);
		
		assertThat(ifaces).containsOnly(ChildIface.class, StandaloneIface.class, StandaloneIface2.class);
	}
	
	@Test
	public void retunrsWholeInheritanceTreeWithoutDuplicates() {
		Collection<Class<?>> ifaces = TypeUtil.interfaceTree(GrandGrandChildIface.class);
		
		assertThat(ifaces)
			.hasSize(6)
			.containsOnly(GrandGrandChildIface.class, GrandChildIface.class, ChildIface.class, ChildIface2.class, StandaloneIface.class, StandaloneIface2.class);
	}
}
