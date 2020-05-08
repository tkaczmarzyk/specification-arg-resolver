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

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Tomasz Kaczmarzyk
 */
public class TypeUtil {

	public static Collection<Class<?>> interfaceTree(Class<?> iface) {
		if (!iface.isInterface()) {
			throw new IllegalArgumentException(iface + " is not an interface!");
		}
		return interfaceTree(iface, new HashSet<Class<?>>());
	}
	
	private static Collection<Class<?>> interfaceTree(Class<?> iface, Collection<Class<?>> accumulator) {
		accumulator.add(iface);
		
		for (Class<?> parentIface : iface.getInterfaces()) {
			accumulator.addAll(interfaceTree(parentIface));
		}
		
		return accumulator;
	}
}
